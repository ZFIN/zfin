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

import groovy.transform.Field
import groovy.xml.slurpersupport.GPathResult
import org.zfin.properties.ZfinProperties

ZfinProperties.init("${System.getenv()['ZFIN_PROPERTIES_PATH']}")

DBNAME = System.getenv("DBNAME")
CANDIDATES_FILE = new File("missingFigureImageCandidates.txt")
REPORT_FILE = new File("missingFigureImagesReport.csv")
SUMMARY_FILE = new File("missingFigureImagesSummary.txt")

@Field final Set<String> IMAGE_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'tif', 'tiff', 'webp'] as Set

// Statuses, in the order the summary lists them.
@Field final String FILLABLE = 'FILLABLE'
@Field final String NO_PMC_FIGURE = 'NO_PMC_FIGURE_WITH_LABEL'
@Field final String AMBIGUOUS = 'AMBIGUOUS_LABEL'
@Field final String IMAGE_NOT_IN_PACKAGE = 'IMAGE_NOT_IN_S3_PACKAGE'
@Field final String NO_PACKAGE = 'NO_OPEN_ACCESS_PACKAGE'
@Field final String NO_FIGURE_NUMBER = 'NO_FIGURE_NUMBER'
@Field final String PMC_ERROR = 'PMC_LOOKUP_FAILED'

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
        def s3Files = PubmedUtils.listS3Files(pmcId).collect { it.substring(it.indexOf('/') + 1) }
        if (s3Files.isEmpty()) {
            figures.each { rows << row(it, NO_PACKAGE) }
            return
        }
        def pmcFigures = pmcFiguresOf(PubmedUtils.getFullText(pmcId.replace("PMC", "")))
        def byKey = pmcFigures.groupBy { it.key }
        figures.each { f ->
            def key = normalizeLabel(f.label)
            def matches = key ? (byKey[key] ?: []) : []
            if (!key) {
                rows << row(f, NO_FIGURE_NUMBER)
            } else if (matches.isEmpty()) {
                rows << row(f, NO_PMC_FIGURE)
            } else if (matches.size() > 1) {
                rows << row(f, AMBIGUOUS, matches*.label.join(' / '))
            } else {
                def m = matches[0]
                def file = fileInPackage(m.href, s3Files)
                rows << (file ? row(f, FILLABLE, m.label, file, m.caption.length())
                              : row(f, IMAGE_NOT_IN_PACKAGE, m.label, m.href))
            }
        }
    } catch (Exception e) {
        println "PMC lookup failed for $pub ($pmcId): ${e.message}"
        figures.each { rows << row(it, PMC_ERROR, '', '', 0, e.message) }
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
[FILLABLE, NO_PMC_FIGURE, AMBIGUOUS, IMAGE_NOT_IN_PACKAGE, NO_PACKAGE, NO_FIGURE_NUMBER, PMC_ERROR].each { s ->
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

/**
 * "Fig. 3" / "Figure 3" / "FIGURE 3." -> "3"; "Fig. S1" / "S1 File" / "Supplementary
 * Figure S1" -> "S1"; sub-figures "Figure 3—figure supplement 1" / "Fig. 3 S1" -> "3-S1".
 * Tables, videos, movies and author response images are not figures to fill, and
 * neither is "text only": all -> null. Without that, "Video 1" or "Fig. Table 1"
 * would compete with (or be mistaken for) Figure 1.
 */
static String normalizeLabel(String label) {
    def s = (label ?: '').trim().toLowerCase().replaceAll(/[.:]+$/, '')
    if (s =~ /table|video|movie|author response/) {
        return null
    }
    def words = s.replaceAll(/[^a-z0-9 ]/, ' ')
    def m = words =~ /\b(s?)(\d+)\b/
    if (!m.find()) {
        return null
    }
    boolean supplementary = m.group(1) || (s.substring(0, m.start()) =~ /\bsupp\w*/)
    // Nature-style "Extended Data Fig. 4" is a different figure from "Figure 4".
    boolean extendedData = s.substring(0, m.start()) =~ /\bextended data\b/
    def key = (supplementary ? 'S' : extendedData ? 'E' : '') + Integer.parseInt(m.group(2))
    // A sub-figure of a main figure: "...figure supplement 1" or a trailing "S1".
    def sub = words.substring(m.end()) =~ /\b(?:supplement\w*\s+|s)(\d+)\b/
    if (!supplementary && sub.find()) {
        key += '-S' + Integer.parseInt(sub.group(1))
    }
    return key
}

/**
 * Figures from the article XML: <fig> elements, plus supplementary figures, which
 * PMC carries as <supplementary-material>. Only image files count there, and not
 * tables, videos or data files -- "S1 Table" would otherwise also normalize to S1.
 */
List<Map> pmcFiguresOf(GPathResult article) {
    def figures = []
    article.'**'.findAll { it.name() == 'fig' }.each { fig ->
        def label = fig.'*'.find { it.name() == 'label' }?.text()?.trim() ?: ''
        def caption = whitespace(fig.'*'.find { it.name() == 'caption' }?.text() ?: '')
        def graphic = fig.'**'.find { it.name() == 'graphic' }
        figures << [label: label, key: normalizeLabel(label), href: graphic ? href(graphic) : null, caption: caption]
    }
    article.'**'.findAll { it.name() == 'supplementary-material' }.each { sm ->
        def label = sm.'*'.find { it.name() == 'label' }?.text()?.trim() ?: ''
        def file = href(sm) ?: sm.'**'.find { it.name() == 'media' }?.with { href(it) }
        if (!file || !IMAGE_EXTENSIONS.contains(extension(file))) {
            return
        }
        if (label =~ /(?i)table|video|movie|data|audio/) {
            return
        }
        def key = normalizeLabel(label)
        def caption = whitespace(sm.'*'.find { it.name() == 'caption' }?.text() ?: '')
        figures << [label: label, key: key ? 'S' + key.replaceFirst(/^S/, '') : null, href: file, caption: caption]
    }
    return figures
}

/** The xlink:href of a node, whatever prefix the parser kept on the attribute name. */
static String href(node) {
    node.attributes().find { k, v -> k.toString().endsWith('href') }?.value
}

static String extension(String file) {
    file.contains('.') ? file.substring(file.lastIndexOf('.') + 1).toLowerCase() : ''
}

/** The package file for an XML image reference; PMC often omits the extension. */
String fileInPackage(String href, List<String> files) {
    if (!href) {
        return null
    }
    def base = href.substring(href.lastIndexOf('/') + 1)
    def exact = files.find { it.substring(it.lastIndexOf('/') + 1) == base }
    if (exact) {
        return exact
    }
    def stem = base.contains('.') ? base.substring(0, base.lastIndexOf('.')) : base
    return files.find {
        def name = it.substring(it.lastIndexOf('/') + 1)
        name.contains('.') && name.substring(0, name.lastIndexOf('.')) == stem && IMAGE_EXTENSIONS.contains(extension(name))
    }
}

static String whitespace(String s) {
    s.replaceAll(/\s+/, ' ').trim()
}

static String csv(value) {
    def s = value == null ? '' : value.toString()
    s.contains(',') || s.contains('"') || s.contains('\n') ? '"' + s.replace('"', '""') + '"' : s
}
