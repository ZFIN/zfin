#!/bin/bash

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
  from marker join ortholog on mrkr_zdb_id == ortho_zebrafish_gene_zdb_id
  where mrkr_zdb_id[1,8] == 'ZDB-GENE'
    and mrkr_abbrev like "%:%"
 ;
 select count(mrkr_zdb_id) uninf_si_orth
  from marker join ortholog on mrkr_zdb_id == ortho_zebrafish_gene_zdb_id
  where mrkr_zdb_id[1,8] == 'ZDB-GENE'
    and mrkr_abbrev like "si:%"
 ;
 select count(mrkr_zdb_id) uninf_zgc_orth
  from marker join ortholog on mrkr_zdb_id == ortho_zebrafish_gene_zdb_id
 where mrkr_zdb_id[1,8] == 'ZDB-GENE'
    and mrkr_abbrev like "zgc:%"
;
END

)> result.log
