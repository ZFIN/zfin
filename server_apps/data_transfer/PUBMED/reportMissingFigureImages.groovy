#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

/**
 * ZFIN-10520, phase 1: report which existing figures that have no image could be
 * filled from PubMed Central. Read-only -- nothing is downloaded or written to the
 * database.
 *
 * Candidates are figures with no image and no caption in publications that have a
 * PMC ID and may show images. These are typically figures a curator created by hand
 * because the original figure load (getPDFandImages.groovy) never brought them in;
 * that loader only handles publications with no figures at all, and only ever
 * inserts new figures, so it cannot fill these.
 *
 * For each publication the script reads the PMC article XML (OAI) for figure and
 * supplementary-figure labels, captions and image file names, lists the PMC
 * open-access S3 package, and matches PMC figures to ZFIN figures by normalized
 * label ("Fig. 3" = "Figure 3" = "Fig 3" -> 3; "Fig. S1" = "S1 File" = "Supplementary
 * Figure S1" -> S1).
 *
 * Usage: reportMissingFigureImages.groovy [ZDB-PUB-...] ...
 *   With publication IDs, only those are checked; otherwise every candidate is.
 *   MAX_PUBS (env) caps the number of publications, for trial runs.
 *
 * Output (working directory):
 *   missingFigureImagesReport.csv  one row per candidate figure
 *   missingFigureImagesSummary.txt counts by status
 */

import org.zfin.properties.ZfinProperties

ZfinProperties.init("${System.getenv()['ZFIN_PROPERTIES_PATH']}")

DBNAME = System.getenv("DBNAME")
CANDIDATES_FILE = new File("missingFigureImageCandidates.txt")
REPORT_FILE = new File("missingFigureImagesReport.csv")
SUMMARY_FILE = new File("missingFigureImagesSummary.txt")

def pubIds = (args as List).findAll { it }
def pubFilter = pubIds ? "AND f.fig_source_zdb_id IN ('" + pubIds.join("','") + "')" : ""

// Candidate figures: no image, no caption, in a PMC publication that may show images.
CANDIDATES_FILE.delete()
PubmedUtils.psql(DBNAME, """
  \\copy (
  SELECT f.fig_source_zdb_id, p.pub_pmc_id, f.fig_zdb_id, f.fig_label
    FROM figure f
    JOIN publication p ON p.zdb_id = f.fig_source_zdb_id
   WHERE p.pub_pmc_id IS NOT NULL AND p.pub_pmc_id <> ''
     AND p.pub_can_show_images
     AND (f.fig_caption IS NULL OR f.fig_caption = '')
     AND NOT EXISTS (SELECT 1 FROM image i WHERE i.img_fig_zdb_id = f.fig_zdb_id)
     $pubFilter
   ORDER BY f.fig_source_zdb_id, f.fig_label
  ) to '${CANDIDATES_FILE.absolutePath}' with (format csv, delimiter E'\\t');
""").waitFor()

def candidatesByPub = new LinkedHashMap<String, List<Map>>()
CANDIDATES_FILE.eachLine { line ->
    def c = line.split('\t', -1)
    candidatesByPub.computeIfAbsent(c[0]) { [] } << [pub: c[0], pmc: c[1], fig: c[2], label: c[3]]
}

def maxPubs = System.getenv("MAX_PUBS")?.toInteger()
def pubs = candidatesByPub.keySet().toList()
if (maxPubs) {
    pubs = pubs.take(maxPubs)
}
println "Candidate figures: ${candidatesByPub.values().sum { it.size() } ?: 0} in ${candidatesByPub.size()} publications; checking ${pubs.size()}."

def rows = []
pubs.eachWithIndex { pub, i ->
    def figures = candidatesByPub[pub]
    def pmcId = figures[0].pmc
    try {
        def article = PmcFigureMatcher.article(pmcId)
        figures.each { f ->
            def m = PmcFigureMatcher.match(f.label, article)
            rows << row(f, m.status, m.figure?.label ?: '', m.file ?: (m.figure?.href ?: ''),
                    m.status == PmcFigureMatcher.FILLABLE ? m.figure.caption.length() : 0)
        }
    } catch (Exception e) {
        println "PMC lookup failed for $pub ($pmcId): ${e.message}"
        figures.each { rows << row(it, PmcFigureMatcher.PMC_ERROR, '', '', 0, e.message) }
    }
    if ((i + 1) % 50 == 0) {
        println "  ${i + 1} / ${pubs.size()} publications"
    }
}

REPORT_FILE.withWriter { w ->
    w.writeLine(['publication', 'pmc_id', 'figure_zdb_id', 'figure_label', 'status',
                 'pmc_label', 'pmc_file', 'pmc_caption_length', 'note'].join(','))
    rows.each { r -> w.writeLine(r.collect { csv(it) }.join(',')) }
}

def byStatus = rows.groupBy { it[4] }
def summary = []
summary << "ZFIN-10520 missing figure images -- ${new Date()}"
summary << "Publications checked: ${pubs.size()}  candidate figures: ${rows.size()}"
PmcFigureMatcher.STATUSES.each { s ->
    summary << String.format("  %-28s %6d figures in %5d publications",
            s, (byStatus[s] ?: []).size(), ((byStatus[s] ?: [])*.get(0) as Set).size())
}
SUMMARY_FILE.text = summary.join('\n') + '\n'
println SUMMARY_FILE.text
println "Report: ${REPORT_FILE.absolutePath}"

// ---------------------------------------------------------------------------

def row(Map f, String status, String pmcLabel = '', String pmcFile = '', int captionLength = 0, String note = '') {
    [f.pub, f.pmc, f.fig, f.label, status, pmcLabel, pmcFile, captionLength, note]
}

static String csv(value) {
    def s = value == null ? '' : value.toString()
    s.contains(',') || s.contains('"') || s.contains('\n') ? '"' + s.replace('"', '""') + '"' : s
}
