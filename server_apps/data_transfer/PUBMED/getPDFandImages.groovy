#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?
import groovy.util.slurpersupport.GPathResult

import groovy.xml.StreamingMarkupBuilder
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

DBNAME = System.getenv("DBNAME")
PUB_IDS_TO_CHECK = "pdfsNeeded.txt"
PUBS_WITH_PDFS_TO_UPDATE = new File ("pdfsAvailable.txt")
FIGS_TO_LOAD = new File ("figsToLoad.txt")
final WORKING_DIR = new File("${ZfinPropertiesEnum.TARGETROOT}/server_apps/data_transfer/PUBMED")

WORKING_DIR.eachFileMatch(~/pdfs.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/fig.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/loadSQL.*\.txt/) { it.delete() }

Date date = new Date()
// go back 14 days to slurp up stragglers.
def dateToCheck = date - 14
def idsToGrab = [:]
String datePart = dateToCheck.format("yyyy-MM-dd")
String timePart = dateToCheck.format("HH:mm:ss")


PubmedUtils.dbaccess DBNAME, """
  \\copy (
  SELECT pub_pmc_id, zdb_id
  FROM publication
  WHERE pub_pmc_id IS NOT NULL
     AND NOT EXISTS (SELECT 'x' 
                    FROM publication_file 
                    WHERE pf_pub_zdb_id = zdb_id 
                    AND pf_file_type_id =1) )to '$PUB_IDS_TO_CHECK' delimiter ',';
"""

def downloadPMCBundle(String url, String zdbId) {
    //TODO: check if folder already exists, handle that
    directory = new File("${System.getenv()['LOADUP_FULL_PATH']}/$zdbId").mkdir()

    //TODO: add the Pdf file name to publciation_file
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

def processPMCText(GPathResult pmcTextArticle, String zdbId) {
    def art = pmcTextArticle.GetRecord.record.metadata.article
    def markedUpBody = new StreamingMarkupBuilder().bindNode(art.body).toString()
    def figMatches = markedUpBody =~ /<tag0:fig id=(.*?)>(.*?)<\/tag0:fig>/
    def imageFilePath = "${System.getenv()['LOADUP_FULL_PATH']}/$zdbId/"
    figMatches.each {
        def entireFigString = it[0]
        def labelMatch = entireFigString =~ /<tag0:label>(.*?)<\/tag0:label>/
        def label = labelMatch[0][1]
        def captionMatch = entireFigString =~ /<tag0:caption>(.*?)<\/tag0:caption>/
        def caption = captionMatch[0][1].toString().replaceAll('tag0:', '')
        def imageNameMatch = entireFigString =~ /<tag0:graphic id='(.*?)' xlink:href='(.*?)'/
        def image = imageNameMatch[0][2] + ".jpg"
        FIGS_TO_LOAD.append([zdbId, pmcId, imageFilePath, label, caption, image + ".jpg"].join('|') + "\n")
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
                downloadPMCBundle(pdfPath, zdbId)
                def fullTxt = PubmedUtils.getFullText(pmcId.toString().substring(3))
                processPMCText(fullTxt)
                println pmcId
                def art = fullTxt.GetRecord.record.metadata.article
                def markedUpBody = new StreamingMarkupBuilder().bindNode(art.body).toString()
                def figMatches = markedUpBody =~ /<tag0:fig id=(.*?)>(.*?)<\/tag0:fig>/
                figMatches.each {
                    def entireFigString = it[0]
                    def label = entireFigString =~ /<tag0:label>(.*?)<\/tag0:label>/
                    println "here's the label: " + label[0][1]
                    def caption = entireFigString =~ /<tag0:caption>(.*?)<\/tag0:caption>/
                    println "here's the caption: " + caption[0][1].toString().replaceAll('tag0:','')
                    def imageName = entireFigString =~ /<tag0:graphic id='(.*?)' xlink:href='(.*?)'/
                    //<tag0:graphic id='d29e816' xlink:href='41375_2018_226_Fig1_HTML' xmlns:xlink='http://www.w3.org/1999/xlink'></tag0:graphic>
                    println "here's the image name: " + imageName[0][2] +".jpg"
                    FIGS_TO_LOAD.append([zdbId,pmcId,"${System.getenv()['LOADUP_FULL_PATH']}/$zdbId/",label[0][1], caption[0][1].toString().replaceAll('tag0:',''), imageName[0][2] +".jpg"].join('|')+"\n")

                }

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

FIGS_TO_LOAD.delete()