#! /local/bin/bash

# for testing just till the "collapse.awk" propagates to /private...
#COMMONS_BIN=/research/zusers/tomc/Projects/TRUNK/ZFIN_WWW/commons/bin
COMMONS_BIN=/private/ZfinLinks/Commons/bin

(${INFORMIXDIR}/bin/dbaccess -a <!--|DB_NAME|--> << END

 select "          count" type from single;

 select count(mrkr_zdb_id) uninf_all from marker
  where mrkr_zdb_id[1,8] == 'ZDB-GENE'
    and mrkr_abbrev like "%:%"
 ;
 select count(mrkr_zdb_id) uninf_si from marker
 where mrkr_zdb_id[1,8] == 'ZDB-GENE'
    and mrkr_abbrev like "si:%"
 ;
 select count(mrkr_zdb_id) uninf_zgc from marker
  where mrkr_zdb_id[1,8] == 'ZDB-GENE'
    and mrkr_abbrev like "zgc:%"
 ;
 select count(mrkr_zdb_id) uninf_all_orth
  from marker join orthologue on mrkr_zdb_id == c_gene_id
  where mrkr_zdb_id[1,8] == 'ZDB-GENE'
    and mrkr_abbrev like "%:%"
 ;
 select count(mrkr_zdb_id) uninf_si_orth
  from marker join orthologue on mrkr_zdb_id == c_gene_id
  where mrkr_zdb_id[1,8] == 'ZDB-GENE'
    and mrkr_abbrev like "si:%"
 ;
 select count(mrkr_zdb_id) uninf_zgc_orth
  from marker join orthologue on mrkr_zdb_id == c_gene_id
 where mrkr_zdb_id[1,8] == 'ZDB-GENE'
    and mrkr_abbrev like "zgc:%"
;
END
)2> /dev/null|/usr/bin/grep -v "^$" |\
${COMMONS_BIN}/collapse.awk 2 |\
awk 'BEGIN{print "Subject: Uninformative Gene Nomenclature with/without Orthology\n\n"}{print}'|\
mail -t <!--|COUNT_VEGA_OUT|-->
