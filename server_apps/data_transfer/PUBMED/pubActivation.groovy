#!/bin/bash
//private/apps/groovy/bin/groovy -cp "<!--|GROOVY_CLASSPATH|-->:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

DBNAME = System.getenv("DBNAME")
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

batchSize = 2000
count = 0
println("Fetching pubs from PubMed")

new File(ACTIVATED_PUBS).withWriter { output ->
    new File(PUB_IDS_TO_CHECK).withReader { reader ->
        def lines = reader.iterator()
        while (lines.hasNext()) {
            ids = lines.take(batchSize).collect { it.split(",")[0] }
            articleSet = PubmedUtils.getFromPubmed(ids)
            count += articleSet.PubmedArticle.size()
            articleSet.PubmedArticle.each { article ->
                pubmedId = article.MedlineCitation.PMID
                status = article.PubmedData.PublicationStatus
                if (status == 'ppublish' || status == 'epublish') {
                    output.writeLine([pubmedId, status].join(","))
                    if (article.PubmedData.ArticleIdList.ArticleId.size() > 0) {
                        article.PubmedData.ArticleIdList.ArticleId.each { articleId ->
                            if (articleId.@IdType == 'pmc') {
                                pmcId = articleId
                                PMC_ID_PUBS.append([pubmedId, pmcId, "pmc"].join(",")+"\n")
                            }
                            if (articleId.@IdType == 'mid') {
                                mId = articleId
                                PMC_ID_PUBS.append([pubmedId, mId, "mid"].join(",")+"\n")
                            }
                        }
                    }
                }
            }
            println("Fetched $count pubs")
        }
    }
}

PubmedUtils.dbaccess DBNAME, """
 BEGIN WORK;

  CREATE TEMP TABLE tmp_activation (
    pmid integer,
    status text
  );

  \\copy tmp_activation FROM '$ACTIVATED_PUBS' null '' delimiter ',';

  CREATE TEMP TABLE tmp_pmcid_update (pmid integer,
   altId text,
   idType text );
   
   \\copy tmp_pmcid_update FROM '$PMC_ID_PUBS' null '' delimiter ',';

  UPDATE publication
    SET pub_pmc_id = (SELECT distinct altId from tmp_pmcid_update where idType = 'pmc' and pmid = accession_no)
    WHERE EXISTS (SELECT 'x' FROM tmp_pmcid_update WHERE accession_no = pmid)
    and pub_pmc_id is null ;
    
   UPDATE publication
    SET pub_mid = (SELECT distinct altId from tmp_pmcid_update where idType = 'mid' and pmid = accession_no)
    WHERE EXISTS (SELECT 'x' FROM tmp_pmcid_update WHERE accession_no = pmid)
    and pub_mid is null ;   

  COMMIT WORK;
"""

new File(PUB_IDS_TO_CHECK).delete()
//new File(ACTIVATED_PUBS).delete()
PMC_ID_PUBS.delete()