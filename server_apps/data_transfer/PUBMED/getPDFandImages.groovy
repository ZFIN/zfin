#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?
import groovy.util.slurpersupport.GPathResult

import groovy.xml.StreamingMarkupBuilder
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum
import groovy.io.FileType

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

DBNAME = System.getenv("DBNAME")
PUB_IDS_TO_CHECK = "pdfsNeeded.txt"
PUBS_WITH_PDFS_TO_UPDATE = new File ("pdfsAvailable.txt")
FIGS_TO_LOAD = new File ("figsToLoad.txt")
PUB_FILES_TO_LOAD = new File ("pdfsToLoad.txt")
ADD_BASIC_PDFS_TO_DB = new File ("pdfBasicFilesToLoad.txt")
final WORKING_DIR = new File("${ZfinPropertiesEnum.TARGETROOT}/server_apps/data_transfer/PUBMED")

WORKING_DIR.eachFileMatch(~/pdfs.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/fig.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/loadSQL.*\.txt/) { it.delete() }

Date date = new Date()
// go back 14 days to slurp up stragglers.
def dateToCheck = date - 5
def idsToGrab = [:]
String datePart = dateToCheck.format("yyyy-MM-dd")
String timePart = dateToCheck.format("HH:mm:ss")


PubmedUtils.psql DBNAME, """
  \\copy (
  SELECT pub_pmc_id, zdb_id
  FROM publication
  WHERE pub_pmc_id IS NOT NULL
     AND NOT EXISTS (SELECT 'x' 
                    FROM publication_file 
                    WHERE pf_pub_zdb_id = zdb_id 
                    AND pf_file_type_id =1) ) to '$PUB_IDS_TO_CHECK' delimiter ',';
"""

def addSummaryPDF(String zdbId, String pmcId) {
    def list = []
    def dir = new File("${System.getenv()['LOADUP_FULL_PATH']}/$zdbId/")
    dir.eachFileRecurse (FileType.FILES) { file ->
        if (file.name.endsWith('pdf')){
            println file.path
            println file.name
            ADD_BASIC_PDFS_TO_DB.append([zdbId, pmcId, zdbId+"/"+file.name].join('|') + "\n")
        }
    }
}


def downloadPMCFileBundle(String url, String zdbId) {
    def directory = new File ("${System.getenv()['LOADUP_FULL_PATH']}/$zdbId")
    if (!directory.exists()) {
        directory.mkdir()
    }

    def file = new FileOutputStream("${System.getenv()['LOADUP_FULL_PATH']}/$zdbId/$zdbId"+".tar.gz")
    def out = new BufferedOutputStream(file)
    out << new URL(url).openStream()
    out.close()

    def gziped_bundle = "${System.getenv()['LOADUP_FULL_PATH']}/$zdbId/$zdbId"+".tar.gz"
    def unzipped_output = "${System.getenv()['LOADUP_FULL_PATH']}/$zdbId/$zdbId"+".tar"
    File unzippedFile = new File(unzipped_output)
    if (!unzippedFile.exists()){
        PubmedUtils.gunzip(gziped_bundle, unzipped_output)
    }

    def cmd = "cd "+ "${System.getenv()['LOADUP_FULL_PATH']}/$zdbId/ " + "&& /bin/tar -xf *.tar --strip 1"
    ["/bin/bash", "-c", cmd].execute().waitFor()

}

def processPMCText(GPathResult pmcTextArticle, String zdbId, String pmcId) {
    def art = pmcTextArticle.GetRecord.record.metadata.article
    def markedUpBody = new StreamingMarkupBuilder().bindNode(art.body).toString()
    def supplimentMatches = markedUpBody =~ /<tag0:supplementary-material content-type='(.*?)<\/tag0:supplementary-material>/
    if (supplimentMatches.size() > 0) {
        supplimentMatches.each {
            def entireSupplementMatch = it[0]
            def filenameMatch = supplimentMatches =~ /<tag0:media xlink:href='(.*?)'/
            if (filenameMatch.size() > 0) {
                def filename = filenameMatch[0][1]
                PUB_FILES_TO_LOAD.append([zdbId, pmcId, zdbId+"/"+filename].join('|') + "\n")
            }

        }
    }
    addSummaryPDF(zdbId, pmcId)
    def figMatches = markedUpBody =~ /<tag0:fig id=(.*?)>(.*?)<\/tag0:fig>/
    def imageFilePath = "${System.getenv()['LOADUP_FULL_PATH']}/$zdbId/"
    if (figMatches.size() >0) {
        figMatches.each {
            def entireFigString = it[0]
            def labelMatch = entireFigString =~ /<tag0:label>(.*?)<\/tag0:label>/
            def label
            def caption
            def image
            if (labelMatch.size() >0 ) {
                label = labelMatch[0][1]
            }
            def captionMatch = entireFigString =~ /<tag0:caption>(.*?)<\/tag0:caption>/
            if (captionMatch.size() >0) {
                caption = captionMatch[0][1].toString().replaceAll('tag0:', '')
            }
            def imageNameMatch = entireFigString =~ /<tag0:graphic id='(.*?)' xlink:href='(.*?)'/
            if (imageNameMatch.size()) {
                image = imageNameMatch[0][2] + ".jpg"
            }
            FIGS_TO_LOAD.append([zdbId, pmcId, zdbId+"/"+imageFilePath, label, caption, image + ".jpg"].join('|') + "\n")
        }
    }
    //TODO: add the Pdf file name to publciation_file
}


def processPMCFileBundle(GPathResult oa, Map idsToGrab, File PUBS_WITH_PDFS_TO_UPDATE) {
    oa.records.record.each { rec ->
        def pmcId = rec.@id.text()

        if (idsToGrab.containsKey(pmcId)) {
            if (rec.link.@format.text() == 'tgz') {
                def pdfPath = rec.link.@href.text()
                PUBS_WITH_PDFS_TO_UPDATE.append(pdfPath + "\n")
                def zdbId = idsToGrab.get(pmcId)
                downloadPMCFileBundle(pdfPath, zdbId)
                def fullTxt = PubmedUtils.getFullText(pmcId.toString().substring(3))
                println pmcId +","+zdbId
                processPMCText(fullTxt, zdbId, pmcId)
            }
        }
    }
    def resumeToken = oa.records.resumption.link.@token.text()
    if (resumeToken != "") {
        def moreRecords = PubmedUtils.getResumptionSet(oa.records.resumption.link.@token.text())
        processPMCFileBundle(moreRecords, idsToGrab, PUBS_WITH_PDFS_TO_UPDATE)
    }
}

new File(PUB_IDS_TO_CHECK).withReader { reader ->
    def lines = reader.iterator()
    lines.each { String line ->
        row = line.split(',')
        idsToGrab.put(row[0], row[1])
    }
}


def pmcFileBundleRecords
pmcFileBundleRecords = PubmedUtils.getPDFandImagesTarballsByDate(datePart+"+"+timePart)

processPMCFileBundle(pmcFileBundleRecords, idsToGrab, PUBS_WITH_PDFS_TO_UPDATE)

loadFigsAndImages = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/load_figs_and_images.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
loadFigsAndImages.waitFor()

loadPubFiles = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/load_pub_files.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
loadPubFiles.waitFor()

loadBasicPDFFiles = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/add_basic_pdfs.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
loadBasicPDFFiles.waitFor()