#!/bin/bash
//private/apps/groovy/bin/groovy -cp "<!--|GROOVY_CLASSPATH|-->:." "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

DBNAME = System.getenv("DBNAME")
PUB_IDS_TO_CHECK = "pubsToActivate.txt"
ACTIVATED_PUBS = "activatedPubs.txt"
def pmcPubs = new File ("pmcPubs.txt")

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
                                pmcPubs.write([pubmedId, pmcId, "pmc"].join(",")+"\n")
                            }
                            if (articleId.@IdType == 'mid') {
                                mId = articleId
                                pmcPubs.write([pubmedId, mid, "mid"].join(",")+"\n")
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

  CREATE TEMP TABLE tmp_id_update (pmid integer,
   altId text,
   idType text );
   
   \\copy tmp_id_update FROM '$PMC_PUBS' null '' delimiter ',';

  UPDATE publication
    SET pub_pmc_id = (SELECT altId from tmp_id_update where itType = 'pmc')
    WHERE EXISTS (SELECT 'x' FROM tmp_id_update WHERE accession_no = pmid);
    
  UPDATE publication
    SET pub_mid = (SELECT altId from tmp_id_update where itType = 'mid')
    WHERE EXISTS (SELECT 'x' FROM tmp_id_update WHERE accession_no = pmid);  
    

  COMMIT WORK;
"""

new File(PUB_IDS_TO_CHECK).delete()
new File(ACTIVATED_PUBS).delete()
//new File(PMC_PUBS).delete()