#!/bin/sh

echo 'drop trigger locus_insert_trigger' | dbaccess $DBNAME ;
echo 'drop trigger fish_insert_trigger' | dbaccess $DBNAME ;
echo 'drop trigger alteration_insert_trigger' | dbaccess $DBNAME ;
echo 'drop trigger fish_pheno_keywords_update_trigger' | dbaccess $DBNAME ;
echo 'drop trigger fish_allele_update_trigger' | dbaccess $DBNAME ;
echo 'drop trigger alteration_allele_update_trigger' | dbaccess $DBNAME ;

dbaccess $DBNAME /research/zcentral/www_homes/swirl/lib/DB_triggers/locus_insert_trigger.sql ;
dbaccess $DBNAME /research/zcentral/www_homes/swirl/lib/DB_triggers/fish_insert_trigger.sql ;
dbaccess $DBNAME /research/zcentral/www_homes/swirl/lib/DB_triggers/alteration_insert_trigger.sql ;
dbaccess $DBNAME /research/zcentral/www_homes/swirl/lib/DB_triggers/alteration_allele_update_trigger.sql ;
dbaccess $DBNAME /research/zcentral/www_homes/swirl/lib/DB_triggers/fish_allele_update_trigger.sql ;
dbaccess $DBNAME /research/zcentral/www_homes/swirl/lib/DB_triggers/fish_pheno_keywords_update_trigger.sql ;
