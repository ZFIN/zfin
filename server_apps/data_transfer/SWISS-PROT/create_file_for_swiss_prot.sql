-- Creates file to be sent to S-P.  S-P then generates a file that they return to us.  It is processed by the scripts in this directory. 

UNLOAD to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/swiss_prot.txt' 
  DELIMITER "	"
  select distinct linked_recid , mrkr_abbrev, acc_num from db_link, marker
    where linked_recid = mrkr_zdb_id
    and DB_name = 'SWISS-PROT'
    order by 1;


