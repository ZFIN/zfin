-- update_withdrawn_dblink.sql

-- change withdrawn transcripts dblinks 
-- to point to local withdrawn transcript blastdb 
-- 'ZDB-FDBCONT-060417-1'  -- Vega_trans


begin work;

! echo "If a transcript is flagged as withdrawn "
! echo "make it's ottdarT dblink point to Vega_Withdrawn"
update db_link set dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-100114-1' -- Vega_Withdrawn (59)
 where dblink_fdbcont_zdb_id != 'ZDB-FDBCONT-100114-1' 
   and exists (
	select 't' from transcript
	 where dblink_acc_num == tscript_load_id
	   and tscript_status_id == 1
	   and tscript_load_id[1,8] == 'OTTDART0'
)
;

! echo "Are any ottdarT pointing at the withdrawn blastdb, but not flagged as withdrawn?"
select count(*) bezero
 from db_link join transcript on  dblink_acc_num == tscript_load_id
 where dblink_fdbcont_zdb_id == 'ZDB-FDBCONT-100114-1' 
   and tscript_status_id != 1
;   
 
--rollback work;
