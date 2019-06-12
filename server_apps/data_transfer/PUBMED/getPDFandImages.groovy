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
WORKING_DIR.eachFileMatch(~/.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/fig.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/loadSQL.*\.txt/) { it.delete() }

DBNAME = System.getenv("DBNAME")
PUB_IDS_TO_CHECK = "pdfsNeeded.txt"
PUBS_WITH_PDFS_TO_UPDATE = new File ("pdfsAvailable.txt")
FIGS_TO_LOAD = new File ("figsToLoad.txt")
PUB_FILES_TO_LOAD = new File ("pdfsToLoad.txt")
ADD_BASIC_PDFS_TO_DB = new File ("pdfBasicFilesToLoad.txt")
PUBS_TO_GIVE_PERMISSIONS = new File ("pubsToGivePermission.txt")
MOVIES_TO_LOAD = new File ("moviesToLoad.txt")


Date date = new Date()
// go back two weeks to slurp up stragglers.


def idsToGrab = [:]

PubmedUtils.psql DBNAME, """
  \\copy (
  SELECT pub_pmc_id, zdb_id
  FROM publication
  WHERE pub_pmc_id IS NOT NULL and pub_pmc_id != ''
     AND NOT EXISTS (SELECT 'x' 
                    FROM publication_file 
                    WHERE pf_pub_zdb_id = zdb_id 
                    AND pf_file_type_id =1)   
     AND NOT EXISTS (SELECT 'x' 
                        FROM pub_tracking_history, pub_tracking_status
                        WHERE pth_pub_zdb_id = zdb_id
                        and pth_status_id = pts_pk_id
                        AND pts_status = 'CLOSED')
     ) to '$PUB_IDS_TO_CHECK' delimiter ',';
"""

def addSummaryPDF(String zdbId, String pmcId, pubYear) {

    def dir = new File("${System.getenv()['LOADUP_FULL_PATH']}/pubs/$pubYear/$zdbId/")

    dir.eachFileRecurse (FileType.FILES) { file ->
        if (file.name.endsWith('pdf')){
            ADD_BASIC_PDFS_TO_DB.append([zdbId, pmcId, pubYear+"/"+zdbId+"/"+file.name].join('|') + "\n")
        }
    }

}

def downloadPMCFileBundle(String url, String zdbId, String pubYear) {
    def timeStart = new Date()
    def yearDirectory = new File ("${System.getenv()['LOADUP_FULL_PATH']}/pubs/$pubYear/")
    def directory = new File ("${System.getenv()['LOADUP_FULL_PATH']}/pubs/$pubYear/$zdbId")
    if (!yearDirectory.exists()){
        yearDirectory.mkdir()
    }
    if (!directory.exists()) {
        directory.mkdir()
    }

    def file = new FileOutputStream("${System.getenv()['LOADUP_FULL_PATH']}/pubs/$pubYear/$zdbId/$zdbId"+".tar.gz")
    def out = new BufferedOutputStream(file)

    out << new URL(url).openStream()
    out.close()
    def timeStop = new Date()
    TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
    println ("download to filesystem duration:" +  duration)

    def gziped_bundle = "${System.getenv()['LOADUP_FULL_PATH']}/pubs/$pubYear/$zdbId/$zdbId"+".tar.gz"
    def unzipped_output = "${System.getenv()['LOADUP_FULL_PATH']}/pubs/$pubYear/$zdbId/$zdbId"+".tar"
    File unzippedFile = new File(unzipped_output)
    if (!unzippedFile.exists()){
        PubmedUtils.gunzip(gziped_bundle, unzipped_output)
    }

    def timeStart2 = new Date()
    def cmd = "cd "+ "${System.getenv()['LOADUP_FULL_PATH']}/pubs/$pubYear/$zdbId/ " + "&& /bin/tar -xf *.tar --strip 1"
    ["/bin/bash", "-c", cmd].execute().waitFor()

    def timeStop2 = new Date()
    TimeDuration duration2 = TimeCategory.minus(timeStop2, timeStart2)
    println ("extract file duration:" +  duration2)
}

def processPMCText(GPathResult pmcTextArticle, String zdbId, String pmcId, String pubYear) {
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
        def supplimentPattern = "<${tag}:supplementary-material content-type=(.*?)</${tag}:supplementary-material>"
        def supplimentMatches = markedUpBody =~ /${supplimentPattern}/
        if (supplimentMatches.size() > 0) {
            println (supplimentMatches.size())
            supplimentMatches.each {
                def filenamePattern = "<${tag}:media xlink:href='(.*?)'"
                def filenameMatch = supplimentMatches =~ /${filenamePattern}/
                if (filenameMatch.size() > 0) {
                    def filename = filenameMatch[0][1]
                    if (filename.endsWith(".avi") || filename.endsWith(".mp4") || filename.endsWith(".mov") || filename.endsWith(".wmv")){
                        MOVIES_TO_LOAD.append([zdbId, pmcId, pubYear + "/" + zdbId + "/" + filename].join('|') + "\n")
                    } else {
                        PUB_FILES_TO_LOAD.append([zdbId, pmcId, pubYear + "/" + zdbId + "/" + filename].join('|') + "\n")
                    }
                }
            }
        }

        addSummaryPDF(zdbId, pmcId, pubYear)
        def figPattern = "<${tag}:fig(.*?)>(.*?)</${tag}:fig>"
        def figMatches = markedUpBody =~ /${figPattern}/

        def imageFilePath = "${System.getenv()['LOADUP_FULL_PATH']}/pubs/$pubYear/$zdbId/"

        if (figMatches.size() > 0) {
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
                        caption = caption.replace(tag+":",'')
                        caption = caption.replaceAll("\\s{2,}", " ")
                        caption = caption.replace("|", "&&&&&")
                    }
                }
                def imagePattern = "<${tag}:graphic(.*?)xlink:href='(.*?)'"
                def imageNameMatch = entireFigString =~ /${imagePattern}/
                if (imageNameMatch.size() > 0) {
                    imageNameMatch.each {
                        image = it[2] + ".jpg"
                        println(label + " " + image)
                    }
                }

                FIGS_TO_LOAD.append([zdbId, pmcId, imageFilePath, label, caption, image].join('|') + "\n")
            }
        }
    }
}

def fetchBundlesForExistingPubs(Map idsToGrab, File PUBS_WITH_PDFS_TO_UPDATE) {

    for (id in idsToGrab) {
        def zdbId = id.value
        def pmcId = id.key
        def pubYearMatch = zdbId =~ /^(ZDB-PUB-)(\d{2})(\d{2})(\d{2})(-\d+)$/
        def pubYear
        if (pubYearMatch.size() > 0) {
            pubYear = pubYearMatch[0][2]
            if (pubYear.toString().startsWith("9")){
                pubYear = "19" + pubYear
            }
            else {
                pubYear = "20" + pubYear
            }
        }
        PubmedUtils.getPdfMetaDataRecord(pmcId).records.record.each { rec ->
            if (rec.link.@format.text() == 'tgz') {

                def pdfPath = rec.link.@href.text()
                PUBS_WITH_PDFS_TO_UPDATE.append(pdfPath + "\n")
                downloadPMCFileBundle(pdfPath, zdbId, pubYear)
                def fullTxt = PubmedUtils.getFullText(pmcId.toString().substring(3))
                println pmcId + "," + zdbId
                processPMCText(fullTxt, zdbId, pmcId, pubYear)
            }
        }
    }
}

new File(PUB_IDS_TO_CHECK).withReader { reader ->
    def lines = reader.iterator()
    lines.each { String line ->
        row = line.split(',')
        idsToGrab.put(row[0], row[1])
    }

}

fetchBundlesForExistingPubs(idsToGrab, PUBS_WITH_PDFS_TO_UPDATE)

givePubsPermissions = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/give_pubs_permissions.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
givePubsPermissions.waitFor()

loadFigsAndImages = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/load_figs_and_images.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
loadFigsAndImages.waitFor()

loadFigsAndMovies = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/load_figs_and_movies.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
loadFigsAndMovies.waitFor()

loadPubFiles = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/load_pub_files.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
loadPubFiles.waitFor()

loadBasicPDFFiles = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/add_basic_pdfs.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
loadBasicPDFFiles.waitFor()
