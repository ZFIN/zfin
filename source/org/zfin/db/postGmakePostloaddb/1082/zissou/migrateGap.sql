--liquibase formatted sql
--changeset sierra:migrateGap

create temp table tmp_insert (id varchar(50))
with no log;

insert into tmp_insert (id)
 select zdb_id from publication
 where status = 'active'
and not exists (Select 'x' from pub_tracking_history
    	       	       where pth_pub_zdb_id = zdb_id);




set triggers for pub_tracking_history disabled;

insert into pub_tracking_history (pth_pub_zdb_id, 
       	    			 pth_status_id,  
				 pth_status_set_by, 
				 pth_status_insert_date) 
select id, 
       1, 
       "ZDB-PERS-030612-1", 
       current year to second
    from tmp_insert;


delete from tmp_insert;

insert into tmp_insert (id)
 select zdb_id 
   from publication, pub_tracking_history a
   where a.pth_pub_zdb_id = zdb_id
    and a.pth_location_id is not null
 and not exists (Select 'x' from pub_tracking_history b, pub_tracking_status
     	 		where b.pth_pub_zdb_id = zdb_id
			and b.pth_status_id = pts_pk_id
			and pts_status = 'INDEXED');

insert into pub_tracking_history (pth_pub_zdb_id, 
       	    			 pth_status_id,  
				 pth_status_set_by, 
				 pth_status_insert_date) 
select id, 
       (Select pts_pk_id from pub_tracking_status where pts_status = "INDEXED"), 
       "ZDB-PERS-100329-1", 
       current year to second
    from tmp_insert;

set triggers for pub_tracking_history enabled;

update publication
 set pub_indexed_date = (select max(pth_status_insert_date)
     		      		from pub_tracking_history, pub_tracking_status
				where pth_pub_zdb_id = zdb_id
				and pth_status_id = pts_pk_id
				and pts_status = 'INDEXED')
 where pub_indexed_date is null
 and exists (Select 'x' from pub_tracking_history
     	    	   where pth_pub_zdb_id = zdb_id);
