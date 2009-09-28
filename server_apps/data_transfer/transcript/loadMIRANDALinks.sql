begin work ;

create temp table tmp_load (tl_sangerName varchar(30),
       	    	  	   tl_accNum varchar(30),
			   geneName varchar(30))
with no log ;

load from v5.txt.danio_rerio.unl
  insert into tmp_load;


create temp table tmp_dblinks (accNum varchar(30))
with no log;

insert into tmp_dblinks
  select distinct t1_sangerName from tmp_load;

update tmp_dblinks
  replace (accNum, 'dre-miR-',);

set constraints all deferred ;
insert into db_link (dblink_zdb_id, dblink_acc_num, dblink_linked_recid,
       	    	    		    dblink_fdbcont_zdb_id)
select get_id('DBLINK',accNum, (select mrkr_Zdb_id from marker
       			       	       	where 'mirn'||accNum = mrkr_abbrev),
				(select fdbcont_zdb_id 
                                   from foreign_db_contains, foreign_db 
					where fdbcont_fdb_db_id = fdb_db_pk_id
					and fdb_db_name = 'MIRANDA')
)
  from tmp_dblinks;

insert into zdb_active_data
  select dblink_zdb_id 
    from db_link
    where not exists (Select 'x' from zdb_active_data
    	      	     	     where zactvd_zdb_id = dblink_zdb_id);

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select dblink_zdb_id,  'ZDB-PUB-081217-13'
    from db_link
   where exists (Select 'x' from foreign_db_contains, foreign_db 
					where fdbcont_fdb_db_id = fdb_db_pk_id
					and fdb_db_name = 'MIRANDA');
    	      	     	      
set constraints all immediate;

--commit work ;
rollback work ;

