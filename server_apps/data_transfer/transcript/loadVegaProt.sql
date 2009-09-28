begin work ;

create temp table tmp_prot_load (ottdarp varchar(50),ottdarg varchar(50),
       	    	  			 ottdart varchar(50))
		with no log ;

load from ottdarp.unl 
  insert into tmp_prot_load;

set constraints all deferred;

insert into db_link (dblink_zdb_id, dblink_acc_num, dblink_linked_recid,
       	    	    		    dblink_fdbcont_zdb_id)
  select get_id('DBLINK'), 
  	 ottdarp,
	 mrkr_zdb_id,
	 'ZDB-FDBCONT-'
    from tmp_prot_load, marker
    where ottdart = mrkr_name ;

insert into record_Attribution (Recattrib_data_zdb_id, recattrib_source_zdb_id)
  select dblink_zdb_id, 'ZDB-PUB-030703-1'
   from db_link
   where dblink_acc_num like 'OTTDARP%'
   and dblink_linked_recid like 'ZDB-TSCRIPT%';

insert into zdb_active_data
  select dblink_zdb_id
    from db_link
    where not exists (select 'x' from zdb_active_data
    	      	     	     	 where zactvd_zdb_id = dblink_zdb_id);

set constraints all immediate ;

--commit work ;

rollback work ;