#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?

/**
 * This script will download PDFs and images from PubMed.
 * By default, it uses the database to find publications that need PDFs and images.
 * If publication ZDB ID(s) are provided as command line arguments, it will only check for those.
 */

import groovy.io.FileType
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder

import org.apache.commons.io.FilenameUtils
import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

ZfinProperties.init("${System.getenv()['ZFIN_PROPERTIES_PATH']}")

final WORKING_DIR = new File("${ZfinPropertiesEnum.TARGETROOT}/server_apps/data_transfer/PUBMED")
WORKING_DIR.eachFileMatch(~/.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/.*\.csv/) { it.delete() }
WORKING_DIR.eachFileMatch(~/fig.*\.txt/) { it.delete() }
WORKING_DIR.eachFileMatch(~/loadSQL.*\.txt/) { it.delete() }

DBNAME = System.getenv("DBNAME")
PUB_IDS_TO_CHECK = "pdfsNeeded.txt"
PUBS_WITH_PDFS_TO_UPDATE = new File("pdfsAvailable.txt")
FIGS_TO_LOAD = new File("figsToLoad.txt")
PUB_FILES_TO_LOAD = new File("pdfsToLoad.txt")
ADD_BASIC_PDFS_TO_DB = new File("pdfBasicFilesToLoad.txt")
PUBS_TO_GIVE_PERMISSIONS = new File("pubsToGivePermission.txt")
NON_OPEN_PUBS = new File("nonOpenAccessPubs.csv")

def idsToGrab = [:]

//if the environment variable PUB_ZDB_IDs exists or we are passed command line arguments, use those IDs to query the DB instead
def specificPubZdbIDs = System.getenv("PUB_ZDB_IDs")?.split("[,\\s]+").findAll { it != null && it != ""}
if (args.length > 0) {
    specificPubZdbIDs = Arrays.asList(args)
}

//if an argument is provided, use that to query the DB instead
if (specificPubZdbIDs.size() > 0) {

    pubZdbIDs = "'" + specificPubZdbIDs.join("','") + "'"

    println "Publication ZDB ID (s) provided, checking for PDFs for $pubZdbIDs."

    PubmedUtils.psql DBNAME, """
      \\copy (
      SELECT pub_pmc_id, zdb_id
      FROM publication
      WHERE zdb_id in ($pubZdbIDs)
         ) to '$PUB_IDS_TO_CHECK' delimiter ',';
    """
} else {
    println "No publication ZDB ID provided as command line argument, checking for all publications using standard logic."
    PubmedUtils.psql DBNAME, """
      \\copy (
      SELECT pub_pmc_id, zdb_id
      FROM publication
      WHERE pub_pmc_id IS NOT NULL and pub_pmc_id != ''
         AND NOT EXISTS (SELECT 'x' 
                        FROM publication_file 
                        WHERE pf_pub_zdb_id = zdb_id 
                        AND pf_file_type_id =1)
         AND NOT EXISTS (select 'x' from figure where fig_source_zdb_id = zdb_id) 
         ) to '$PUB_IDS_TO_CHECK' delimiter ',';
    """
}

def addSummaryPDF(String zdbId, String pmcId, pubYear) {

    def dir = new File("${System.getenv()['LOADUP_FULL_PATH']}/$pubYear/$zdbId/")

    dir.eachFileRecurse(FileType.FILES) { file ->
        if (file.name.endsWith('.pdf')) {
            ADD_BASIC_PDFS_TO_DB.append([zdbId, pmcId, pubYear + "/" + zdbId + "/" + file.name, file.name].join('|') + "\n")
        }
    }

}

def downloadPMCFileBundle(String url, String zdbId, String pubYear) {
    def timeStart = new Date()
    def directoryPath = "${System.getenv()['LOADUP_FULL_PATH']}/$pubYear/$zdbId"
    def filePrefix = "$directoryPath/$zdbId"
    def directory = new File(directoryPath)
    directory.mkdirs()

    def tarOutputFile = "${filePrefix}.tar.gz"
    println("Writing to $tarOutputFile")
    new File(tarOutputFile).withOutputStream { it << new URL(url).openStream() }

    def timeStop = new Date()
    TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
    println("download to filesystem duration:" + duration)

    def unzipped_output = "${filePrefix}.tar"
    File unzippedFile = new File(unzipped_output)
    if (!unzippedFile.exists()) {
        PubmedUtils.gunzip(tarOutputFile, unzipped_output)
    }

    def timeStart2 = new Date()
    def cmd = "cd $directoryPath && /bin/tar -xf *.tar --strip 1"
    ["/bin/bash", "-c", cmd].execute().waitFor()

    def timeStop2 = new Date()
    TimeDuration duration2 = TimeCategory.minus(timeStop2, timeStart2)
    println("extract file duration:" + duration2)
}

def downloadPDF (String downloadUrl, String pmcId, String zdbId, String pubYear) {

    def timeStart = new Date()
    def directory = new File("${System.getenv()['LOADUP_FULL_PATH']}/$pubYear/$zdbId")
    def filePath = "$directory/${zdbId}.pdf"
    def ncbiUrl = "https://www.ncbi.nlm.nih.gov/pmc/articles/${pmcId.toString().replace("PMC","")}/"
    def zfinUrl = "https://zfin.org/${zdbId}"

    directory.mkdirs()

    def successfulDownload = downloadPdfFromFtp(downloadUrl, filePath)
    def timeStop = new Date()
    TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
    println("PDF download to filesystem duration:" + duration)
    if (!successfulDownload) {
        println("Could not download the PDF from the FTP site for $pmcId")
        if (!NON_OPEN_PUBS.exists() || NON_OPEN_PUBS.length() == 0) {
            NON_OPEN_PUBS.append("pmcId,zdbId,ncbiUrl,zfinUrl\n")
        }
        NON_OPEN_PUBS.append([pmcId, zdbId, ncbiUrl, zfinUrl].join(',') + "\n")
    }
    def mimetype = "/usr/bin/file -b --mime-type $filePath".execute().text.trim()
    if (!mimetype.equals("application/pdf")) {
        println("The file downloaded from PMC for $pmcId is not a PDF, it is a $mimetype. Deleting the file.")
        new File("$filePath").delete()
    } else {
        println("successfully downloaded the PDF from the FTP site for $pmcId")
        ADD_BASIC_PDFS_TO_DB.append([zdbId, pmcId, pubYear + "/" + zdbId + "/" + zdbId + ".pdf", zdbId + ".pdf"].join('|') + "\n")
    }
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
    def imageFilePath = "${System.getenv()['LOADUP_FULL_PATH']}/$pubYear/$zdbId/"
    def tagMatch = markedUpBody =~ /<([^\/]*?):body/

    if (tagMatch.size() == 1) {

        def tag = tagMatch[0][1]  // extract the XML namespace tag pattern for use in extracting supplements, figures, images downstream.

        def supplimentPattern = "<${tag}:supplementary-material content-type=(.*?)</${tag}:supplementary-material>"
        def supplimentMatches = markedUpBody =~ /${supplimentPattern}/
        if (supplimentMatches.size() > 0) {
            supplimentMatches.each {
                def supplement = it
                if (pmcId == 'PMC:4679720') {
                    println(it)
                }
                def filenamePattern = "<${tag}:media xlink:href='(.*?)'"
                def filenameMatch = supplimentMatches =~ /${filenamePattern}/
                if (filenameMatch.size() > 0) {
                    def filename = filenameMatch[0][1]
                    if (filename.endsWith(".avi") || filename.endsWith(".mp4") || filename.endsWith(".mov") || filename.endsWith(".wmv")) {
                        parseLabelCaptionImage(supplement,zdbId,pmcId,imageFilePath,pubYear, tag)
                        println("videos")
                        println(filename)
                    } else {
                        PUB_FILES_TO_LOAD.append([zdbId, pmcId, pubYear + "/" + zdbId + "/" + filename, filename].join('|') + "\n")
                    }
                }
            }
        }
        // extract figures one by one from the grouping 'fig' tags
        def figPattern = "<${tag}:fig(.*?)>(.*?)</${tag}:fig>"
        def figMatches = markedUpBody =~ /${figPattern}/

        if (figMatches.size() > 0) {
            figMatches.each {
                def entireFigString = it[0]
                parseLabelCaptionImage(entireFigString, zdbId, pmcId, imageFilePath, pubYear, tag)
            }
        }
        else { // means the publisher pulls its figures into one section of the XML under the tag 'floats-group'
            def floatsGroup = new StreamingMarkupBuilder().bindNode(article["floats-group"]).toString()
            def fgFigPattern = "<${tag}:fig(.*?)>(.*?)</${tag}:fig>"
            def fgFigMatches = floatsGroup =~ /${fgFigPattern}/
            println("floatsGroup images")

            if (fgFigMatches.size() > 0) {
                fgFigMatches.each {
                    def entireFigString = it[0]
                    parseLabelCaptionImage(entireFigString, zdbId, pmcId, imageFilePath, pubYear, tag)
                }
            }
        }
        addSummaryPDF(zdbId, pmcId, pubYear)
    }
}

def parseLabelCaptionImage(groupMatchString, zdbId, pmcId, imageFilePath, pubYear, tag) {

    def entireFigString = groupMatchString
    def label = ''
    def caption = ''
    def image = ''
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
            caption = caption.replace(tag + ":", '')
            caption = caption.replaceAll("\\s{2,}", " ")
            caption = caption.replace("|", "&&&&&")
        }
    }
    def imagePattern = "<${tag}:graphic(.*?)xlink:href='(.*?)'"
    def imageNameMatch = entireFigString =~ /${imagePattern}/

    if (imageNameMatch.size() > 0) {
        imageNameMatch.each {
            image = it[2]
            // Only append .jpg if the filename doesn't already have an extension
            if (!FilenameUtils.getExtension(image)) {
                image = image + ".jpg"
            }
            println (image)
            String fileNameNoExtension = FilenameUtils.removeExtension(image)
            makeThumbnailAndMediumImage(image, fileNameNoExtension, zdbId, pubYear)
            String extension = FilenameUtils.getExtension(image)
            String thumbnailFilename = fileNameNoExtension + "_thumb" + FilenameUtils.EXTENSION_SEPARATOR + extension
            String mediumFileName = fileNameNoExtension + "_medium" + FilenameUtils.EXTENSION_SEPARATOR + extension
            FIGS_TO_LOAD.append([zdbId, pmcId, image, label, caption, pubYear + "/" + zdbId + "/" + image,
                                 pubYear + "/" + zdbId + "/" + thumbnailFilename,
                                 pubYear + "/" + zdbId + "/" + mediumFileName].join('|') + "\n")
        }
    }
}

def makeThumbnailAndMediumImage(fileName, fileNameNoExtension, pubZdbId, pubYear) {

    String extension = FilenameUtils.getExtension(fileName)

    String thumbnailFilename = fileNameNoExtension + "_thumb" + FilenameUtils.EXTENSION_SEPARATOR + extension
    String mediumFileName = fileNameNoExtension + "_medium" + FilenameUtils.EXTENSION_SEPARATOR + extension
    File thumbnailFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString()+"/"+pubYear+"/"+pubZdbId+"/", thumbnailFilename)
    File mediumFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString()+"/"+pubYear+"/"+pubZdbId+"/", mediumFileName)
    File fullFile = new File(ZfinPropertiesEnum.LOADUP_FULL_PATH.toString()+"/"+pubYear+"/"+pubZdbId+"/", fileName)

    // make thumbnail and medium images in the same directory as their parent images.
    def thumbnailProc = "/bin/convert -thumbnail 1000x64 ${fullFile} ${thumbnailFile}".execute()
    def thumbnailResult = thumbnailProc.waitFor()
    int thumbnailResultCode = thumbnailResult.intValue()

    def mediumProc = "/bin/convert -thumbnail 500x550 ${fullFile} ${mediumFile}".execute()
    def mediumResult = mediumProc.waitFor()
    int mediumResultCode = mediumResult.intValue()

    if (thumbnailResultCode != 0) {
        println("Error creating thumbnail for " + fullFile.getAbsolutePath())
        println("Command executed: /bin/convert -thumbnail 1000x64 ${fullFile} ${thumbnailFile}")
        println("Error code: " + thumbnailProc.exitValue())
        println("Standard error: ")
        println(thumbnailProc.err.text)
        println("Standard output: ")
        println(thumbnailProc.in.text)
    }
    if (mediumResultCode != 0) {
        println("Error creating medium image for " + fullFile.getAbsolutePath())
        println("Command executed: /bin/convert -thumbnail 500x550 ${fullFile} ${mediumFile}")
        println("Error code: " + mediumProc.exitValue())
        println("Standard error: ")
        println(mediumProc.err.text)
        println("Standard output: ")
        println(mediumProc.in.text)
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
            if (pubYear.toString().startsWith("9")) {
                pubYear = "19" + pubYear
            } else {
                pubYear = "20" + pubYear
            }
        }
        PubmedUtils.getPdfMetaDataRecord(pmcId).records.record.each { rec ->
            println("Processing " + pmcId + " for ZDB ID " + zdbId)

            // Iterate over each link element in the record
            rec.link.each { link ->
                def format = link.@format.text()
                def href = link.@href.text()
                println("Found link to " + href)
                println("Found format " + format)

                if (format == 'tgz') {
                    PUBS_WITH_PDFS_TO_UPDATE.append(href + "\n")
                    downloadPMCFileBundle(href, zdbId, pubYear)
                    def fullTxt = PubmedUtils.getFullText(pmcId.toString().substring(3))
                    println pmcId + "," + zdbId
                    println(href)
                    processPMCText(fullTxt, zdbId, pmcId, pubYear)
                } else if (format == 'pdf') {
                    println "found a PDF to try and download manually " + pmcId + "," + zdbId
                    downloadPDF(href, pmcId, zdbId, pubYear)
                }
            }
        }
    }
}

Boolean downloadPdfFromFtp(String pdfUrl, String fileLocation) {
    new File(fileLocation).withOutputStream { out ->
        URLConnection connection = new URL(pdfUrl).openConnection()
        connection.setRequestProperty("user-agent", "Zebrafish Information Network (ZFIN)")
        out << connection.getInputStream()
    }
    println "Downloaded PDF to " + fileLocation
    return true;
}

new File(PUB_IDS_TO_CHECK).withReader { reader ->
    def lines = reader.iterator()
    lines.each { String line ->
        row = line.split(',')
        idsToGrab.put(row[0], row[1])
    }

}

println("Found " + idsToGrab.size() + " publications to check for PDFs and images.")
fetchBundlesForExistingPubs(idsToGrab, PUBS_WITH_PDFS_TO_UPDATE)

givePubsPermissions = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql -v ON_ERROR_STOP=1  " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/give_pubs_permissions.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
givePubsPermissions.waitFor()

loadBasicPDFFiles = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql -v ON_ERROR_STOP=1 " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/add_basic_pdfs.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
loadBasicPDFFiles.waitFor()

loadFigsAndImages = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql -v ON_ERROR_STOP=1 " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/load_figs_and_images.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
loadFigsAndImages.waitFor()

loadPubFiles = ['/bin/bash', '-c', "${ZfinPropertiesEnum.PGBINDIR}/psql -v ON_ERROR_STOP=1 " +
        "${ZfinPropertiesEnum.DB_NAME} -f ${WORKING_DIR.absolutePath}/load_pub_files.sql " +
        ">${WORKING_DIR.absolutePath}/loadSQLOutput.log 2> ${WORKING_DIR.absolutePath}/loadSQLError.log"].execute()
loadPubFiles.waitFor()

if (NON_OPEN_PUBS.length() > 0) {
    println("The following non-open access publications were not downloaded as PDFs:")
    NON_OPEN_PUBS.eachLine { line ->
        println(line)
    }
    System.exit(2)
}