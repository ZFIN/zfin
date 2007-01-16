#!/bin/sh

# Run this script after loading and postloading for all the 
# freshest Frodo goodnes

echo `/bin/date`;
echo 'change all subfile permissions and gmake frodo dir';
echo `chmod 755 *`;
echo `chmod 755 test_data/*`;
echo `chmod 755 ../../../server_apps/data_transfer/PATO/*`;

echo `/bin/date`;
./trigger_fix.sh ;

echo `/bin/date` '---fix_Df_Tg_locus_names.sql--';
dbaccess $DBNAME fix_Df_Tg_locus_names.sql ;

echo `/bin/date` '---unload_tgcons.sql--';
dbaccess $DBNAME unload_tgcons.sql;

echo `/bin/date` '---parse_tgconstructs.pl--';
./parse_tgconstructs.pl;

echo `/bin/date` '---parse_pheno_keywords.pl--';
./parse_pheno_keywords.pl;

echo `/bin/date` '---translatePheno.pl--';
./translatePheno.pl;

echo `/bin/date` '---translatePheno.pl--';
./translateOSC.pl;

echo `/bin/date` '---translatePheno.pl--';
./translateEUtrans.pl;

echo `/bin/date` '---translatevirus.pl--';
./translatevirus.pl;

echo `/bin/date` '---Frodo_v4.sql ---';
dbaccess $DBNAME Frodo_v4.sql;

#echo `/bin/date` '---load_aliases.sql ---';
#dbaccess $DBNAME load_aliases.sql;

echo `/bin/date` '---make_generic_pato_tables.sql ---';
dbaccess $DBNAME make_generic_pato_tables.sql;

echo `/bin/date` '---make_obo_table.sql ---';
 dbaccess $DBNAME make_obo_table.sql;

echo `/bin/date` '---loadPATO.pl---';
../../../server_apps/data_transfer/PATO/loadpato.pl;

echo `/bin/date` '---merge_data.sql ---';
dbaccess $DBNAME merge_data.sql;

echo `/bin/date` '---update_image.sql ---';
dbaccess $DBNAME update_image.sql;

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

echo `/bin/date` '---fix_sources.sql ---';
dbaccess $DBNAME fix_sources.sql;

echo `/bin/date` '---load_tg_con_relations.sql ---';
dbaccess $DBNAME load_tg_con_relations.sql;

echo `/bin/date` '---testdata_load.sql ---';
dbaccess $DBNAME test_data/testdata_load.sql;

echo `/bin/date` '---translatePhenoTestData.pl--';
./test_data/translatePhenoTestData.pl

echo `/bin/date` '---insert_pheno_test_data.sql ---';
dbaccess $DBNAME test_data/insert_pheno_test_data.sql;

#echo `/bin/date` '---move_unTg_alleles.sql ---';
#dbaccess $DBNAME move_unTg_alleles.sql;

echo `/bin/date` '---drop_tables.sql ---';
dbaccess $DBNAME drop_tables.sql;

#echo `/bin/date` '---create_procedures_functions.sql ---';
#dbaccess $DBNAME create_procedures_functions.sql;

echo `/bin/date` '---fix_dup_apato.sql ---';
dbaccess $DBNAME fix_dup_apato.sql;

echo `/bin/date` '---move_EU_annotations.sql ---';
 dbaccess $DBNAME move_EU_annotations.sql;

echo `/bin/date` '---move_EU_annotations.sql ---';
 dbaccess $DBNAME move_EU2_annotations.sql;

echo `/bin/date` '---fix_wts.sql ---';
dbaccess $DBNAME fix_wts.sql;

echo `/bin/date` '---fix_wts.sql ---';
dbaccess $DBNAME fix_efg.sql;

echo `/bin/date` '---fix_aliases_2.sql ---';
dbaccess $DBNAME fix_aliases_2.sql;

echo `/bin/date` '---update_Feature_notes.sql ---';
dbaccess $DBNAME update_Feature_notes.sql;

#commit work on feature_notes!!!
# check for ovl alias

echo `/bin/date` '---assign_default_annotations.sql ---';
dbaccess $DBNAME assign_default_annotations.sql

echo `/bin/date` '---add_breakpoint.sql ---';
dbaccess $DBNAME add_breakpoint.sql;

echo `/bin/date` '---update_feature_marker_relationships.sql ---';
dbaccess $DBNAME update_feature_marker_relationships.sql

echo `/bin/date` '---update_himuts.sql---';
dbaccess $DBNAME update_himuts.sql;

echo `/bin/date` '---regen_genotype_display.sql---';
dbaccess $DBNAME regen_genotype_display.sql;

echo `/bin/date` '---post_Fv4.sql---';
dbaccess $DBNAME post_Fv4.sql;

echo `/bin/date` '---loadGo.pl---';
../../../server_apps/data_transfer/LoadGO/loadgo.pl;

echo `/bin/date` '---unloadAO.pl---';
cd ../../../
cd server_apps/data_transfer/Anatomy
./unloadAO.pl;

echo 'execute function regen_maps()' | dbaccess $DBNAME;

#echo `/bin/date` '---unloaddb.pl ---';
#/research/zcentral/Commons/bin/unloaddb.pl $DBNAME /research/zunloads/databases/frodo/



echo `/bin/date` ;
