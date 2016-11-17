--liquibase formatted sql
--changeset sierra:migrateGap

insert into pub_tracking_history (pth_pub_zdb_id, 
       	    			 pth_status_id,  
				 pth_status_set_by, 
				 pth_status_insert_date, 
				 pth_status_is_current) 
select zdb_id, 
       pts_pk_id, 
       "ZDB-PERS-030612-1", 
       current year to second, 
       "t" 
    from publication, pub_tracking_status 
    where pts_status = "NEW"
    and status = 'active'
 and not exists (Select 'x' from pub_tracking_history
     	 		where pth_pub_zdb_id = zdb_id);
