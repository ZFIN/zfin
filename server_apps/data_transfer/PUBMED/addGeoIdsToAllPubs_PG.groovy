#!/bin/bash
//private/apps/groovy/bin/groovy -cp "<!--|GROOVY_CLASSPATH|-->:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

DBNAME = System.getenv("DBNAME")
GEO_TO_LOAD = "geoIds.txt"
GEO_PRE = "geoListPre.txt"
GEO_POST = "geoListPost.txt"

PubmedUtils.dbaccess DBNAME, """
 \\copy (
  SELECT pdx_pub_zdb_id, pdx_accession_number
  FROM pub_db_xref
  WHERE pdx_fdbcont_zdb_id = 'ZDB-FDBCONT-070919-1' ) to '$GEO_PRE' delimiter '|';
"""

def searchUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gds&term=\"Danio+rerio\"[Organism]+AND+\"gse\"[Filter]&usehistory=y"
println("Requesting: $searchUrl")
def searchResult = (new XmlSlurper()).parse(searchUrl)
def queryKey = searchResult.QueryKey
def webEnv = searchResult.WebEnv
println("Response:")
println("  QueryKey: $queryKey")
println("  WebEnv: $webEnv")

def summaryUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gds&version=2.0&query_key=$queryKey&WebEnv=$webEnv"
println("Requesting: $summaryUrl")
def summaryResult = (new XmlSlurper()).parse(summaryUrl)
println("Response: Found ${summaryResult.DocumentSummarySet.DocumentSummary.size()} documents")

new File(GEO_TO_LOAD).withWriter { output ->
    summaryResult.DocumentSummarySet.DocumentSummary.each { documentSummary ->
        documentSummary.PubMedIds.int.each { pubmedId ->
            output.writeLine([documentSummary.Accession, pubmedId.text()].join("|"))
        }
    }
}

PubmedUtils.dbaccess DBNAME, """
BEGIN WORK;

CREATE TEMP TABLE tmp_geo (
  accession_number varchar(50),
  pubmed_id integer
);

\\copy tmp_geo from '$GEO_TO_LOAD';

DELETE FROM tmp_geo
  WHERE EXISTS (
    SELECT 'x'
    FROM pub_db_xref
    INNER JOIN publication ON pub_db_xref.pdx_pub_zdb_id = publication.zdb_id
    WHERE pub_db_xref.pdx_accession_number = tmp_geo.accession_number
    AND publication.accession_no = tmp_geo.pubmed_id
  );

SELECT COUNT(*)
  FROM tmp_geo;

INSERT INTO pub_db_xref (pdx_pub_zdb_id, pdx_accession_number, pdx_fdbcont_zdb_id)
  SELECT publication.zdb_id, tmp_geo.accession_number, 'ZDB-FDBCONT-070919-1'
  FROM publication
  INNER JOIN tmp_geo on publication.accession_no = tmp_geo.pubmed_id;

\\copy (
  SELECT pdx_pub_zdb_id, pdx_accession_number
  FROM pub_db_xref
  WHERE pdx_fdbcont_zdb_id = 'ZDB-FDBCONT-070919-1' ) to '$GEO_POST' delimiter '|';

COMMIT WORK;
"""

if (args) {
    preLines = new File(GEO_PRE).collect { it.split("\\|") as List }
    postLines = new File(GEO_POST).collect { it.split("\\|") as List }
    added = postLines - preLines
    new ReportGenerator().with {
        setReportTitle("Report for ${args[0]}")
        includeTimestamp()
        addDataTable("Added ${added.size()} GEO IDs", ["Pub ID", "GEO ID"], added)
        writeFiles(new File("."), "addGeoIdsReport")
    }
}
