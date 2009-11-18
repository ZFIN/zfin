-- unloadZFINuniprotIDs.sql
-- The SQl unloads all UniProt IDs at ZFIN to file allZFINuniprotIDs.unl

begin work ;

unload to allZFINuniprotIDs.unl 
  select distinct dblink_acc_num 
    from db_link, foreign_db_contains, foreign_db 
   where dblink_fdbcont_zdb_id = fdbcont_zdb_id 
     and fdbcont_fdb_db_id = fdb_db_pk_id 
     and fdb_db_name = "UniProtKB";
     
commit work ;
-- rollback work ;