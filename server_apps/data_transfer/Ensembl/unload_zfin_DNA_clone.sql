--unload_zfin_DNA_clone.sql
-- get clone names and zdbids for  Ensembl track

unload to 'zfin_DNA_clone.txt' delimiter '	'
select dblink_acc_num, mrkr_abbrev, mrkr_zdb_id,dblink_length
 from marker join db_link on mrkr_zdb_id == dblink_linked_recid
 where mrkr_type in ("BAC","PAC","FOSMID")
   and dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-040412-36'
order by 1
;

