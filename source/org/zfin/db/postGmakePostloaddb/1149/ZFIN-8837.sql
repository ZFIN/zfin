--liquibase formatted sql
--changeset cmpich:ZFIN-8837.sql

create temp table pub_tracking_history_temp as
select * from pub_tracking_history
where pth_location_id = 17
and pth_status_is_current = 't'
  and not exists(select 'c'
                 from record_attribution
                          left outer join marker on recattrib_data_zdb_id = mrkr_zdb_id
                          left outer join feature on recattrib_data_zdb_id = feature_zdb_id
                 where recattrib_source_zdb_id = pth_pub_zdb_id
                   and (mrkr_zdb_id is not null or feature_zdb_id is not null)
    );

update pub_tracking_history_temp
set pth_location_id = null,
    pth_status_id = 13,
    pth_status_set_by = 'ZDB-PERS-030612-1';

insert into pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_location_id, pth_status_set_by, pth_claimed_by, pth_status_insert_date, pth_status_is_current, pth_status_made_non_current_date, pth_days_in_status)
select pth_pub_zdb_id, pth_status_id, pth_location_id, pth_status_set_by, pth_claimed_by, pth_status_insert_date, pth_status_is_current, pth_status_made_non_current_date, pth_days_in_status
from pub_tracking_history_temp;

