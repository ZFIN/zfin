-- Creates file to be sent to S-P.  S-P then generates a file that 
-- they return to us.
--
-- The hardcoded foreign_db_contains record below is:
--
-- fdbcont_fdbdt_data_type       Polypeptide
-- fdbcont_fdb_db_name           SWISS-PROT
-- fdbcont_organism_common_name  Zebrafish
-- fdbcont_fdbdt_super_type      sequence
-- fdbcont_zdb_id                ZDB-FDBCONT-040412-47
--
-- I am not sure if it better to hardcode the ZDB ID or specify all
-- of the above conditions.

create view swissProt as 
  select distinct dblink_linked_recid, mrkr_abbrev, dblink_acc_num 
    from db_link, marker
   where dblink_linked_recid = mrkr_zdb_id
     and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-47'
    order by dblink_linked_recid;
\copy (select * from swissProt) to '<!--|FTP_ROOT|-->/pub/transfer/Swiss-Prot/swiss_prot.txt' with delimiter as '	' null as '';
drop view swissProt;

