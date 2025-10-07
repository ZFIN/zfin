--liquibase formatted sql
--changeset cmpich:ZFIN-9878

insert into record_attribution (recattrib_data_zdb_id,
                                recattrib_source_zdb_id,
                                recattrib_source_type)
select mrkr_zdb_id, recattrib_source_zdb_id, 'standard'
from marker,
     marker_relationship,
     record_attribution
where mrkr_type = 'CRISPR'
  and mrel_mrkr_1_zdb_id = marker.mrkr_zdb_id
  and mrel_type = 'knockdown reagent targets gene'
  and recattrib_data_zdb_id = mrel_zdb_id
  and not exists(
        select *
        from record_attribution
        where recattrib_data_zdb_id = mrkr_zdb_id
    )
;

