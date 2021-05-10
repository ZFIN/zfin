copy (
select ensm_ensdart_id
from ensdar_mapping
where exists (Select 'x' from db_link where dblink_acc_num like 'ENSDARG%') )  to 'ensemblZfinTscriptsForBlast.txt'  DELIMITER '	';
