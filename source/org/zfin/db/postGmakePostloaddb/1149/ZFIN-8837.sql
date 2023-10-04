--liquibase formatted sql
--changeset cmpich:ZFIN-8837.sql

select *
from pub_tracking_history
where pth_location_id = 17;

update pub_tracking_history
set pth_status_id = 13
where pth_location_id = 17
  and not exists(select 'c'
                 from record_attribution
                          left outer join marker on recattrib_data_zdb_id = mrkr_zdb_id
                          left outer join feature on recattrib_data_zdb_id = feature_zdb_id
                 where recattrib_source_zdb_id = pth_pub_zdb_id
                   and (mrkr_zdb_id is not null or feature_zdb_id is not null)
    );
