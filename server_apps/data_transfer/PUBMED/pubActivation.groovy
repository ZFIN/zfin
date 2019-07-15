#!/bin/bash
//opt/misc/groovy/bin/groovy -cp "<!--|GROOVY_CLASSPATH|-->:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

DBNAME = System.getenv("DBNAME")
//
//def scriptPath = "${System.getenv()['TARGETROOT']}/server_apps/data_transfer/PUBMED/complete_auther_names.pl"
//def command = "perl -w $scriptPath"
//println command.execute().text
//
//def scriptPath2 = "${System.getenv()['TARGETROOT']}/server_apps/DB_maintenance/pub_check_and_addback_volpg.pl"
//def command2 = "perl -w $scriptPath2"
//println command2.execute().text


PUB_IDS_TO_CHECK = "pubsToActivate.txt"
ACTIVATED_PUBS = "listOfActivatedPubs.txt"
new File(ACTIVATED_PUBS).delete()
def PMC_ID_PUBS = new File ("pmcIdPubs.txt")
def MID_PUBS = new File  ("mIdPubs.txt")

PubmedUtils.dbaccess DBNAME, """
  \\copy (
  SELECT accession_no, zdb_id
  FROM publication
  WHERE accession_no IS NOT NULL
  and status != 'active' ) to '$PUB_IDS_TO_CHECK' delimiter ',';
"""

def batchSize = 2000
count = 0
def ids= []
def idsToUpdate = [:]
println("Fetching pubs from PubMed")

new File(ACTIVATED_PUBS).withWriter { output ->
    new File(PUB_IDS_TO_CHECK).withReader { reader ->
        def lines = reader.iterator()
        lines.each { String line ->
            def row = line.split(',')
            idsToUpdate.put(row[0], row[1])
            ids.add(row[0])
        }
        def articleSet = PubmedUtils.getFromPubmed(ids)
        articleSet.PubmedArticle.each { article ->
            def pubmedId = article.MedlineCitation.PMID
            def zdbId = idsToUpdate["$pubmedId"]
            def status = article.PubmedData.PublicationStatus
            if (status == 'ppublish' || status == 'epublish') {
                count++
                output.writeLine([zdbId, pubmedId].join(","))
                if (article.PubmedData.ArticleIdList.ArticleId.size() > 0) {
                    article.PubmedData.ArticleIdList.ArticleId.each { articleId ->
                        if (articleId.@IdType == 'pmc') {
                            def pmcId = articleId
                            PMC_ID_PUBS.append([zdbId, pmcId, "pmc"].join(",") + "\n")
                        }
                        if (articleId.@IdType == 'mid') {
                            def mId = articleId
                            PMC_ID_PUBS.append([zdbId, mId, "mid"].join(",") + "\n")
                        }
                        if (articleId.@IdType == 'doi') {
                            def doi = articleId
                            PMC_ID_PUBS.append([zdbId, doi, "doi"].join(",") + "\n")
                        }
                    }
                }
            }
        }
        println("Activated $count pubs")
    }
}

PubmedUtils.dbaccess DBNAME, """
 BEGIN WORK;

  CREATE TEMP TABLE tmp_activation (
    zdbId text,
    pmid integer
  );

  \\copy tmp_activation FROM '$ACTIVATED_PUBS' null '' delimiter ',';

  UPDATE publication
    SET status = 'active' 
    WHERE EXISTS (select 'x' from tmp_activation where pmid = accession_no);

  CREATE TEMP TABLE tmp_pmcid_update (
    zdbId text,  
   altId text,
   idType text );
   
   \\copy tmp_pmcid_update FROM '$PMC_ID_PUBS' null '' delimiter ',';

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

    UPDATE publication
    SET pub_doi = (SELECT distinct altId from tmp_pmcid_update where idType = 'doi' and zdbId = zdb_id)
    WHERE EXISTS (SELECT 'x' FROM tmp_pmcid_update WHERE zdbId = zdb_id)
    and pub_doi is null ; 
    


  COMMIT WORK;
"""

new File(PUB_IDS_TO_CHECK).delete()
PMC_ID_PUBS.delete()
