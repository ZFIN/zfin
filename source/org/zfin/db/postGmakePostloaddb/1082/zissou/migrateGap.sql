--liquibase formatted sql
--changeset sierra:migrateGap

create temp table tmp_insert (id varchar(50))
with no log;

insert into tmp_insert (id)
 select zdb_id from publication
 where status = 'active'
and not exists (Select 'x' from pub_tracking_history
    	       	       where pth_pub_zdb_id = zdb_id);

insert into pub_tracking_history (pth_pub_zdb_id, 
       	    			 pth_status_id,  
				 pth_status_set_by, 
				 pth_status_insert_date) 
select id, 
       1, 
       "ZDB-PERS-030612-1", 
       current year to second
    from tmp_insert;
