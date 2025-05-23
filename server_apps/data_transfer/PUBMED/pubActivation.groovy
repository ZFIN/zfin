#!/bin/bash
//opt/misc/groovy/bin/groovy -cp "<!--|GROOVY_CLASSPATH|-->:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.properties.ZfinPropertiesEnum

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
final WORKING_DIR = new File("${ZfinPropertiesEnum.TARGETROOT}/server_apps/data_transfer/PUBMED")
WORKING_DIR.eachFileMatch(~/.*\.txt/) { it.delete() }
DBNAME = System.getenv("DBNAME")

println("Running complete_auther_names.pl at " + new Date())
def workingDir = "${System.getenv()['TARGETROOT']}/server_apps/data_transfer/PUBMED"
def scriptPath = "${workingDir}/complete_auther_names.pl"
def command = "perl -w $scriptPath"
println command.execute(null, new File(workingDir)).text

println("Running pub_check_and_addback_volpg.pl at " + new Date())
def scriptPath2 = "${System.getenv()['TARGETROOT']}/server_apps/DB_maintenance/pub_check_and_addback_volpg.pl"
def command2 = "perl -w $scriptPath2"
println command2.execute().text

PUB_IDS_TO_CHECK = "pubsThatNeedActivation.txt"
PUBS_TO_ACTIVATE = new File("pubsToActivate.txt")
REPORT_ACTIVATED_PUBS = new File("listOfActivatedPubs.txt")
def PMC_ID_PUBS = new File ("pmcIdPubsActive.txt")

PubmedUtils.dbaccess DBNAME, """
  \\copy (
  SELECT accession_no, zdb_id
  FROM publication
  WHERE accession_no IS NOT NULL
  and status != 'active' ) to '$PUB_IDS_TO_CHECK' delimiter ',';
"""

println("Got the list of pubs to check and stored in $PUB_IDS_TO_CHECK")
println("File size: " + new File(PUB_IDS_TO_CHECK).length())

count = 0
toActivateCount = 0
def ids= []
def idsToUpdate = [:]
println("Fetching pubs from PubMed")

new File(PUB_IDS_TO_CHECK).withReader { reader ->
    def lines = reader.iterator()
    lines.each { String line ->
        row = line.split(',')
        idsToUpdate.put(row[0], row[1])
        ids.add(row[0])
    }
}

println("Number of pubs to check: " + ids.size())
println("Contents of idsToUpdate: \n" + idsToUpdate)


println("Fetching articles from PubMed at " + new Date())
def articleSet = PubmedUtils.getFromPubmed(ids)

toActivateCount += articleSet.PubmedArticle.size()
println("Number of articles fetched: " + toActivateCount)

articleSet.PubmedArticle.each { article ->
    def pubmedId = article.MedlineCitation.PMID
    def zdbId = idsToUpdate["$pubmedId"]
    def status = article.PubmedData.PublicationStatus
    if (status == 'ppublish' || status == 'epublish') {
        count++
        REPORT_ACTIVATED_PUBS.append([zdbId, pubmedId].join("   ")+"\n")
        PUBS_TO_ACTIVATE.append([zdbId, pubmedId].join(",")+"\n")
        article.PubmedData.ArticleIdList.ArticleId.each { articleId ->
            if (articleId.@IdType == 'pmc') {
                def pmcId = articleId
                PMC_ID_PUBS.append(zdbId + ',' + pmcId + ',' + 'pmc' + '\n')
            }
            if (articleId.@IdType == 'mid') {
                def mId = articleId
                PMC_ID_PUBS.append(zdbId + ',' + mId  + ',' + 'mId' +  '\n')
            }
            if (articleId.@IdType == 'doi') {
                def doi = articleId
                PMC_ID_PUBS.append(zdbId + ',' + doi  + ',' + 'doi' +  '\n')
            }
        }
    }
}
//this console output is requested specifically by curators.
println("")
println("")
println("ctInactivePubs = $toActivateCount")
println("ctUpdated = $count")
println("")
println("")

builtQuery = ""

builtQuery += """
 BEGIN WORK;

  CREATE TEMP TABLE tmp_activation (
    zdbId text,
    pmid integer
  );
  
"""

//check if PUBS_TO_ACTIVATE exists
if (PUBS_TO_ACTIVATE.exists() ) {
    builtQuery += """
        \\copy tmp_activation FROM '$PUBS_TO_ACTIVATE' null '' delimiter ',';
    """
}

builtQuery += """
  UPDATE publication
    SET status = 'active'
    WHERE EXISTS (select 'x' from tmp_activation where pmid = accession_no);

  CREATE TEMP TABLE tmp_pmcid_update (
    zdbId text,
   altId text,
   idType text );
"""

//check if PMC_ID_PUBS exists
if (PMC_ID_PUBS.exists() ) {
    builtQuery += """
        \\copy tmp_pmcid_update FROM '$PMC_ID_PUBS' null '' delimiter ',';
    """
}

builtQuery += """
  UPDATE publication
    SET pub_pmc_id = (SELECT distinct altId from tmp_pmcid_update where idType = 'pmc' and zdbId = zdb_id)
    WHERE EXISTS (SELECT 'x' FROM tmp_pmcid_update WHERE zdbId = zdb_id)
    and pub_pmc_id is null ;

   UPDATE publication
    SET pub_mid = (SELECT distinct altId from tmp_pmcid_update where idType = 'mid' and zdbId = zdb_id)
    WHERE EXISTS (SELECT 'x' FROM tmp_pmcid_update WHERE zdbId = zdb_id)
    and pub_mid is null ;

   INSERT INTO pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_set_by)
       SELECT zdb_id, pts_pk_id, 'ZDB-PERS-170918-1'
          from tmp_activation, publication, pub_tracking_status
           where accession_no = pmid
            and pts_status = 'READY_FOR_PROCESSING';
            
   INSERT INTO updates (submitter_id, rec_id,field_name,new_value,upd_when) 
        SELECT (select zdb_id from person where full_name = 'Pub Activation Script'), zdbId,'status','active',now() 
        from tmp_activation, publication where accession_no = pmid;

    UPDATE publication
    SET pub_doi = (SELECT distinct altId from tmp_pmcid_update where idType = 'doi' and zdbId = zdb_id)
    WHERE EXISTS (SELECT 'x' FROM tmp_pmcid_update WHERE zdbId = zdb_id)
    and pub_doi is null ;

  COMMIT WORK;
"""

println("Running the following query: \n" + builtQuery)
println("Running the query at " + new Date())
PubmedUtils.dbaccess DBNAME, builtQuery
println("Query completed at " + new Date())
