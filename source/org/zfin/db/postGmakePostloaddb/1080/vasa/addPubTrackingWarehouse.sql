--liquibase formatted sql
--changeset sierra:addPubTrackingWarehouse

insert into zdb_flag (zflag_name, zflag_is_on, zflag_last_modified)
 values ('regen_pubtrackingmart','f', current year to second);

create table pub_tracking_generated (ptg_pk_id serial8,
					ptg_pub_zdb_id varchar(50),
					ptg_status varchar(100),
					ptg_days_in_status date,
					ptg_status_is_current boolean)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 1024 next size 1024;

insert into pub_tracking_generated (ptg_pub_zdb_id, ptg_status,ptg_days_in_status, ptg_status_is_current)
  select distinct pth_pub_zdb_id, pts_status, date(pth_status_made_non_current_date) - date(pth_status_insert_date), pth_status_is_current
    from pub_tracking_history, pub_tracking_status
 where pth_status_id = pts_pk_id;   
