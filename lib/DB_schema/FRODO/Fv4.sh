#!/bin/sh

# Run this script after loading and postloading for all the 
# freshest Frodo goodnes

echo `/bin/date` '---parse_pheno_keywords.pl--';
./parse_pheno_keywords.pl;

echo `/bin/date` '---translatePheno.pl--';
./translatePheno.pl;

echo `/bin/date` '---fhist_event.sql ---';
dbaccess $DBNAME fhist_event.sql;

echo `/bin/date` '---p_frel_grpmem_correct.sql ---';
dbaccess $DBNAME p_fmrel_grpmem_correct.sql;

echo `/bin/date` '---Frodo_v4.sql ---';
dbaccess $DBNAME Frodo_v4.sql;

echo `/bin/date` '---merge_data.sql ---';
dbaccess $DBNAME merge_data.sql;

echo `/bin/date` '---move_locusreg.sql ---';
dbaccess $DBNAME move_locusreg.sql;

echo `/bin/date` '---attribute.sql ---';
dbaccess $DBNAME attribute.sql;

echo `/bin/date` '---fix_zdb_ids.sql ---';
dbaccess $DBNAME fix_zdb_ids.sql;

echo `/bin/date` '---last_constraints.sql ---';
dbaccess $DBNAME last_constraints.sql;

echo `/bin/date` '---update_comments.sql ---';
dbaccess $DBNAME update_comments.sql;

echo `/bin/date` ;