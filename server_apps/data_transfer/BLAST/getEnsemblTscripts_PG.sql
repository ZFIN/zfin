begin work;

copy (
select ensm_ensdart_id
from ensdar_mapping
where exists (Select 'x' from db_link where dblink_acc_num like 'ENSDARG%') )  to '<!--|ROOT_PATH|-->/server_apps/data_transfer/BLAST/ensemblZfinTscriptsForBlast.txt';

commit work ;