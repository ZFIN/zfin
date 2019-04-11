#!/bin/bash
//private/apps/groovy/bin/groovy -cp "<!--|GROOVY_CLASSPATH|-->:." "$0" $@; exit $?
import groovy.util.slurpersupport.GPathResult
import org.zfin.properties.ZfinProperties

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

DBNAME = System.getenv("DBNAME")

PUB_IDS_TO_CHECK = "pubsThatNeedPDFs.txt"

Date date = new Date()
def dateToCheck = date - 1000
def idsToGrab
String datePart = dateToCheck.format("yyyy-MM-dd")
String timePart = dateToCheck.format("HH:mm:ss")
println datePart+"+"+timePart

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

def processPMC = {GPathResult oa ->
    row = []
    println oa.responseDate

    oa.records.record.each { rec ->
        pmcId = rec.@id.text()
        if (pmcId == "PMC6361423") {
            println "found PMC6361423"
        }
        if (pmcId in idsToGrab) {
            if (rec.link.@format.text() == 'tgz') {
                pdfPath = rec.link.@href.text()
                println pmcId + " " + pdfPath
            }
        }
    }
    //printer.printRecord(row.collect { col -> col.toString().replace('\n', '\\n') })
}

def pmcRecords

new File(PUB_IDS_TO_CHECK).withReader { reader ->
    def lines = reader.iterator()
    while (lines.hasNext()) {
        idsToGrab = lines.collect { it.split(",")[0] }
    }
}

pmcRecords = PubmedUtils.getPDFandImagesTarball(datePart+"+"+timePart)

processPMC(pmcRecords)

