#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?
import groovy.io.FileType


import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

import groovy.time.*


ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

final WORKING_DIR = new File("${ZfinPropertiesEnum.TARGETROOT}/server_apps/data_transfer/PUBMED")
WORKING_DIR.eachFileMatch(~/pdfs.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/fig.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/loadSQL.*\.txt/) { it.delete() }

DBNAME = System.getenv("DBNAME")
PUB_IDS_TO_CHECK = "pdfsNeeded.txt"
PUBS_WITH_PDFS_TO_UPDATE = new File ("pdfsAvailable.txt")
FIGS_TO_LOAD = new File ("figsToLoad.txt")
PUB_FILES_TO_LOAD = new File ("pdfsToLoad.txt")
ADD_BASIC_PDFS_TO_DB = new File ("pdfBasicFilesToLoad.txt")
PUBS_TO_GIVE_PERMISSIONS = new File ("pubsToGivePermission.txt")


Date date = new Date()
// go back two weeks to slurp up stragglers.
def dateToCheck = date - 5
def idsToGrab = [:]
String datePart = dateToCheck.format("yyyy-MM-dd")
String timePart = dateToCheck.format("HH:mm:ss")


PubmedUtils.psql DBNAME, """
  \\copy (
  SELECT pub_pmc_id, zdb_id
  FROM publication
  WHERE pub_pmc_id IS NOT NULL and pub_pmc_id != ''
     AND NOT EXISTS (SELECT 'x' 
                    FROM publication_file 
                    WHERE pf_pub_zdb_id = zdb_id 
                    AND pf_file_type_id =1) ) to '$PUB_IDS_TO_CHECK' delimiter ',';
"""

def addSummaryPDF(String zdbId, String pmcId) {

    def dir = new File("${System.getenv()['LOADUP_FULL_PATH']}/pubs/$zdbId/")
    dir.eachFileRecurse (FileType.FILES) { file ->
        if (file.name.endsWith('pdf')){
            ADD_BASIC_PDFS_TO_DB.append([zdbId, pmcId, zdbId+"/"+file.name].join('|') + "\n")
        }
    }
    def timeStop = new Date()
    TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
    println ("addSummaryPDF duration:" +  duration)
}

def downloadPMCFileBundle(String url, String zdbId) {

    def timeStart = new Date()
    def directory = new File ("${System.getenv()['LOADUP_FULL_PATH']}/pubs/$zdbId")
    if (!directory.exists()) {
        directory.mkdir()
    }

    def file = new FileOutputStream("${System.getenv()['LOADUP_FULL_PATH']}/pubs/$zdbId/$zdbId"+".tar.gz")
    def out = new BufferedOutputStream(file)
    out << new URL(url).openStream()
    out.close()

    def gziped_bundle = "${System.getenv()['LOADUP_FULL_PATH']}/pubs/$zdbId/$zdbId"+".tar.gz"
    def unzipped_output = "${System.getenv()['LOADUP_FULL_PATH']}/pubs/$zdbId/$zdbId"+".tar"
    File unzippedFile = new File(unzipped_output)
    if (!unzippedFile.exists()){
        PubmedUtils.gunzip(gziped_bundle, unzipped_output)
    }

    def cmd = "cd "+ "${System.getenv()['LOADUP_FULL_PATH']}/pubs/$zdbId/ " + "&& /bin/tar -xf *.tar --strip 1"
    ["/bin/bash", "-c", cmd].execute().waitFor()

    def timeStop = new Date()
    TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
    println ("downloadPMCFileBundle duration:" +  duration)
}

def processPMCText(GPathResult pmcTextArticle, String zdbId, String pmcId) {
    def article = pmcTextArticle.GetRecord.record.metadata.article
    def header = pmcTextArticle.GetRecord.record.header
    header.setSpec.each { setspec ->
        if (setspec == 'npgopen' || setspec == 'pmc-open') {
            PUBS_TO_GIVE_PERMISSIONS.append([zdbId].join('|') + "\n")
        }
    }
    def markedUpBody = new StreamingMarkupBuilder().bindNode(article.body).toString()

    def tagMatch = markedUpBody =~ /<([^\/]*?):body/
    if (tagMatch.size() == 1) {
        def tag = tagMatch[0][1]
//        def supplimentPattern = "<${tag}:supplementary-material content-type=(.*?)</${tag}:supplementary-material>"
//        def supplimentMatches = markedUpBody =~ /${supplimentPattern}/
//        if (supplimentMatches.size() > 0) {
//            supplimentMatches.each {
//                println (supplimentMatches[0][1])
//                def filenamePattern = "<${tag}:media xlink:href='(.*?)'"
//                def filenameMatch = supplimentMatches =~ /${filenamePattern}/
//                if (filenameMatch.size() > 0) {
//                    filename = filenameMatch[0][1]
//                    PUB_FILES_TO_LOAD.append([zdbId, pmcId, zdbId + "/" + filename].join('|') + "\n")
//                }
//            }
//        }

        addSummaryPDF(zdbId, pmcId)
        def figPattern = "<${tag}:fig(.*?)>(.*?)</${tag}:fig>"
        def figMatches = markedUpBody =~ /${figPattern}/

        def imageFilePath = "${System.getenv()['LOADUP_FULL_PATH']}/pubs/$zdbId/"

        if (figMatches.size() > 0) {
            println("matched figures")
            figMatches.each {
                def entireFigString = it[0]
                def label
                def caption
                def image
                def labelPattern = "<${tag}:label>(.*?)</${tag}:label>"
                def labelMatch = entireFigString =~ /${labelPattern}/
                if (labelMatch.size() > 0) {
                    labelMatch.each {
                        label = it[1]
                    }
                }
                def captionPattern = "<${tag}:caption>(.*?)</${tag}:caption>"
                def captionMatch = entireFigString =~ /${captionPattern}/
                if (captionMatch.size() > 0) {
                    captionMatch.each {
                        caption = it[1]
                    }
                }
                def imagePattern = "<${tag}:graphic(.*?)xlink:href='(.*?)'"
                def imageNameMatch = entireFigString =~ /${imagePattern}/
                if (imageNameMatch.size() > 0) {
                    imageNameMatch.each {
                        image = it[2] + ".jpg"
                    }
                }
                FIGS_TO_LOAD.append([zdbId, pmcId, imageFilePath, label, caption, image].join('|') + "\n")
            }
        }
    }
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

givePubsPermissions = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/give_pubs_permissions.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
givePubsPermissions.waitFor()

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
