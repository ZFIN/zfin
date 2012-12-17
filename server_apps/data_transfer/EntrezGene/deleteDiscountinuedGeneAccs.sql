begin work;

set pdqpriority 50;

!echo 'create temp table pre_delete'
create temp table pre_delete (
		pred_dblink_zdb_id		varchar(50),
		pred_discontinued_acc           varchar(50)
)with no log;

!echo 'load from deleteListDblinks'		
load from 'deleteListDblinks' 
   insert into pre_delete;
   
create index pred_data_id_index
 on pre_delete(pred_dblink_zdb_id) using btree in idxdbs3;   


!echo 'delete from record_attribution'
delete from record_attribution
      where recattrib_source_zdb_id = 'ZDB-PUB-020723-3' 
        and exists (select 'x' from pre_delete where recattrib_data_zdb_id = pred_dblink_zdb_id);
		  
!echo 'take off the records that have other sources from the delete list' 
delete from pre_delete
      where exists (select 'x' from record_attribution where recattrib_data_zdb_id = pred_dblink_zdb_id);

update statistics high for table pre_delete;

!echo 'unload to deleteListDblinksDetails' 
unload to deleteListDblinksDetails 
   select * from db_link dblk
    where exists (select 'x' from pre_delete
                   where pred_dblink_zdb_id = dblk.dblink_zdb_id
                     and pred_discontinued_acc = dblk.dblink_acc_num)
       order by dblink_linked_recid;

!echo 'delete from zdb_active_data' 
delete from zdb_active_data
      where exists (select 'x' from pre_delete where pred_dblink_zdb_id = zactvd_zdb_id);

--rollback work;
commit work;


