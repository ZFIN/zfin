-- Creates file to be sent to S-P.  S-P then generates a file that they return to us.  It is processed by the scripts in this directory. 

UNLOAD to '<!--|ROOT_PATH|-->/server_apps/data_transfer/SWISS-PROT/swiss_prot.txt' 
  DELIMITER "	"
  select distinct dblink_linked_recid, mrkr_abbrev, dblink_acc_num 
    from db_link, marker
   where dblink_linked_recid = mrkr_zdb_id
     and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-47'
    order by 1;


