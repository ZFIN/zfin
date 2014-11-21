begin work;

unload to <!--|ROOT_PATH|-->/server_apps/data_transfer/BLAST/ensemblZFTscripts.txt
select ensm_ensdart_id
 from ensdar_mapping
where exists (Select 'x' from db_link where dblink_acc_num like 'ENSDARG%');

commit work ;