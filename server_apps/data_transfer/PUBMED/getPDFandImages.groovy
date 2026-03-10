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
PUBS_WITH_PDFS_TO_UPDATE_FILE = new File("pdfsAvailable.txt")
PUBS_WITH_PDFS_TO_UPDATE = new LinkedHashSet<String>()
FIGS_TO_LOAD_FILE = new File("figsToLoad.txt")
FIGS_TO_LOAD = new LinkedHashSet<String>()
PUB_FILES_TO_LOAD_FILE = new File("pdfsToLoad.txt")
PUB_FILES_TO_LOAD = new LinkedHashSet<String>()
ADD_BASIC_PDFS_TO_DB_FILE = new File("pdfBasicFilesToLoad.txt")
ADD_BASIC_PDFS_TO_DB = new LinkedHashSet<String>()
PUBS_TO_GIVE_PERMISSIONS_FILE = new File("pubsToGivePermission.txt")
PUBS_TO_GIVE_PERMISSIONS = new LinkedHashSet<String>()
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
            ADD_BASIC_PDFS_TO_DB.add([zdbId, pmcId, pubYear + "/" + zdbId + "/" + file.name, file.name].join('|'))
        }
    }

}

private static final Set<String> DOWNLOADABLE_EXTENSIONS = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'tif', 'tiff'] as Set

def downloadS3FilesForArticle(List<String> s3Keys, String zdbId, String pubYear) {
    def timeStart = new Date()
    def directoryPath = "${System.getenv()['LOADUP_FULL_PATH']}/$pubYear/$zdbId"
    new File(directoryPath).mkdirs()

    def keysToDownload = s3Keys.findAll { key ->
        def ext = key.split('\\.').last().toLowerCase()
        DOWNLOADABLE_EXTENSIONS.contains(ext)
    }

    keysToDownload.each { key ->
        def filename = key.split('/').last()
        def localPath = "$directoryPath/$filename"
        println("Downloading S3: $key -> $localPath")
        PubmedUtils.downloadS3File(key, localPath)
    }

    def timeStop = new Date()
    TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
    println("S3 download duration for $zdbId: $duration (${keysToDownload.size()} files)")
}

def recordPdfFromS3(String s3PdfKey, String pmcId, String zdbId, String pubYear) {
    def directoryPath = "${System.getenv()['LOADUP_FULL_PATH']}/$pubYear/$zdbId"
    // The PDF was already downloaded by downloadS3FilesForArticle; rename to match expected convention
    def s3Filename = s3PdfKey.split('/').last()
    def s3FilePath = "$directoryPath/$s3Filename"
    def expectedPath = "$directoryPath/${zdbId}.pdf"

    if (s3Filename != "${zdbId}.pdf") {
        new File(s3FilePath).renameTo(new File(expectedPath))
    }

    def mimetype = "/usr/bin/file -b --mime-type $expectedPath".execute().text.trim()
    if (!mimetype.equals("application/pdf")) {
        println("The file downloaded from S3 for $pmcId is not a PDF, it is a $mimetype. Deleting the file.")
        new File(expectedPath).delete()
    } else {
        println("Successfully downloaded PDF from S3 for $pmcId")
        ADD_BASIC_PDFS_TO_DB.add([zdbId, pmcId, pubYear + "/" + zdbId + "/" + zdbId + ".pdf", zdbId + ".pdf"].join('|'))
    }
}

def recordNonOpenPub(String pmcId, String zdbId) {
    def ncbiUrl = "https://www.ncbi.nlm.nih.gov/pmc/articles/${pmcId.toString().replace("PMC", "")}/"
    def zfinUrl = "https://zfin.org/${zdbId}"
    if (!NON_OPEN_PUBS.exists() || NON_OPEN_PUBS.length() == 0) {
        NON_OPEN_PUBS.append("pmcId,zdbId,ncbiUrl,zfinUrl\n")
    }
    NON_OPEN_PUBS.append([pmcId, zdbId, ncbiUrl, zfinUrl].join(',') + "\n")
}

def processPMCText(GPathResult pmcTextArticle, String zdbId, String pmcId, String pubYear) {
    def article = pmcTextArticle.GetRecord.record.metadata.article
    def header = pmcTextArticle.GetRecord.record.header
    header.setSpec.each { setspec ->
        if (setspec == 'npgopen' || setspec == 'pmc-open') {
            PUBS_TO_GIVE_PERMISSIONS.add(zdbId)
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
                        PUB_FILES_TO_LOAD.add([zdbId, pmcId, pubYear + "/" + zdbId + "/" + filename, filename].join('|'))
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
            FIGS_TO_LOAD.add([zdbId, pmcId, image, label, caption, pubYear + "/" + zdbId + "/" + image,
                                 pubYear + "/" + zdbId + "/" + thumbnailFilename,
                                 pubYear + "/" + zdbId + "/" + mediumFileName].join('|'))
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

def fetchBundlesForExistingPubs(Map idsToGrab) {

    def failedIds = []
    def processedCount = 0

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

        try {
            println("Processing $pmcId for ZDB ID $zdbId")

            // List available files from the PMC Open Access S3 bucket
            def s3Files = PubmedUtils.listS3Files(pmcId)
            if (s3Files.isEmpty()) {
                println("No files found in S3 for $pmcId")
                recordNonOpenPub(pmcId, zdbId)
                processedCount++
                continue
            }
            println("Found ${s3Files.size()} files in S3 for $pmcId: ${s3Files.collect { it.split('/').last() }}")

            // Download all files from S3 (PDF, images, etc.)
            downloadS3FilesForArticle(s3Files, zdbId, pubYear)
            PUBS_WITH_PDFS_TO_UPDATE.add("s3://${s3Files[0].split('/')[0]}")

            // Check for PDF
            def pdfKey = s3Files.find { it.toLowerCase().endsWith('.pdf') }
            if (pdfKey) {
                recordPdfFromS3(pdfKey, pmcId, zdbId, pubYear)
            } else {
                println("No PDF available in S3 for $pmcId")
                recordNonOpenPub(pmcId, zdbId)
            }

            // Fetch full text via OAI for figure/caption metadata and process it
            def numericPmcId = pmcId.toString().replace("PMC", "")
            def fullTxt = PubmedUtils.getFullText(numericPmcId)
            processPMCText(fullTxt, zdbId, pmcId, pubYear)

        } catch (Exception e) {
            println("ERROR processing ${pmcId} (${zdbId}): ${e.message}")
            e.printStackTrace()
            failedIds << [pmcId: pmcId, zdbId: zdbId, error: e.message]
        }

        processedCount++
        // Brief pause between publications to avoid overwhelming services
        if (processedCount % 5 == 0) {
            Thread.sleep(500)
        }
    }

    if (failedIds.size() > 0) {
        println("\n=== FAILED PUBLICATIONS (${failedIds.size()}) ===")
        failedIds.each { println("  ${it.pmcId} (${it.zdbId}): ${it.error}") }
        println("===================================\n")
    }
}

new File(PUB_IDS_TO_CHECK).withReader { reader ->
    def lines = reader.iterator()
    lines.each { String line ->
        row = line.split(',')
        idsToGrab.put(row[0], row[1])
    }

}

println("Found " + idsToGrab.size() + " publications to check for PDFs and images.")
fetchBundlesForExistingPubs(idsToGrab)

// Write deduplicated entries to files for SQL loading
def writeSetToFile = { Set<String> set, File file ->
    file.text = set.collect { it + "\n" }.join('')
}
writeSetToFile(PUBS_WITH_PDFS_TO_UPDATE, PUBS_WITH_PDFS_TO_UPDATE_FILE)
writeSetToFile(ADD_BASIC_PDFS_TO_DB, ADD_BASIC_PDFS_TO_DB_FILE)
writeSetToFile(FIGS_TO_LOAD, FIGS_TO_LOAD_FILE)
writeSetToFile(PUB_FILES_TO_LOAD, PUB_FILES_TO_LOAD_FILE)
writeSetToFile(PUBS_TO_GIVE_PERMISSIONS, PUBS_TO_GIVE_PERMISSIONS_FILE)

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