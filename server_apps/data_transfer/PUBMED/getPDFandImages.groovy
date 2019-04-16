#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?
import groovy.util.slurpersupport.GPathResult
import org.zfin.properties.ZfinProperties

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

DBNAME = System.getenv("DBNAME")
PUB_IDS_TO_CHECK = "pubsThatNeedPDFs.txt"
PUBS_WITH_PDFS_TO_UPDATE = new File ("pdfAvailable.txt")

Date date = new Date()
// go back 14 days to slurp up stragglers.
def dateToCheck = date - 5
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
    //TODO: parse output of full text to get the figure to image matchup
    //TODO: get the figure captions.
    //TODO:: make zdb-ids for figures, images
    //TODO: match up images with new figures in img_file
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
    ["/bin/bash", "-c", cmd].execute()

}



def processPMC(GPathResult oa, Map idsToGrab, File PUBS_WITH_PDFS_TO_UPDATE) {
    oa.records.record.each { rec ->
        def pmcId = rec.@id.text()
        if (idsToGrab.containsKey(pmcId)) {
            if (rec.link.@format.text() == 'tgz') {
                def pdfPath = rec.link.@href.text()
                PUBS_WITH_PDFS_TO_UPDATE.append(pdfPath+"\n")
                def zdbId = idsToGrab.get(pmcId)
                downloadPMCBundle(pdfPath, zdbId)
            }
        }
    }
    def resumeToken = oa.records.resumption.link.@token.text()
    if (resumeToken != ""){
        def moreRecords = PubmedUtils.getResumptionSet(oa.records.resumption.link.@token.text())
        processPMC(moreRecords, idsToGrab, PUBS_WITH_PDFS_TO_UPDATE)
    }
}

def pmcRecords

new File(PUB_IDS_TO_CHECK).withReader { reader ->
    def lines = reader.iterator()
    lines.each { String line ->
        row = line.split(',')
        idsToGrab.put(row[0], row[1])
    }
}


pmcRecords = PubmedUtils.getPDFandImagesTarball(datePart+"+"+timePart)

processPMC(pmcRecords, idsToGrab, PUBS_WITH_PDFS_TO_UPDATE)

//PUBS_WITH_PDFS_TO_UPDATE.delete()
//PUB_IDS_TO_CHECK.delete()