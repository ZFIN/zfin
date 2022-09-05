--liquibase formatted sql
--changeset cmpich:ZFIN-8152

update sequence_feature_chromosome_location
set sfcl_assembly = 'Zv9'
where exists(
              select *
              from marker
              where mrkr_zdb_id = sfcl_feature_zdb_id
                and mrkr_zdb_id ~ 'ZDB-NCCR-190826'
                and sfcl_assembly is null
          )
;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
select sfcl_zdb_id, 'ZDB-PUB-170214-158', 'standard' from sequence_feature_chromosome_location
join marker on mrkr_zdb_id= sfcl_feature_zdb_id
    and mrkr_zdb_id ~ 'ZDB-NCCR-190826';

update sequence_feature_chromosome_location_generated
set sfclg_assembly = 'Zv9'
where exists(
              select *
              from marker
              where mrkr_zdb_id = sfclg_data_zdb_id
                and mrkr_zdb_id ~ 'ZDB-NCCR-190826'
                and sfclg_assembly is null
          )
;

select count(*)
from sequence_feature_chromosome_location
         join marker on mrkr_zdb_id = sfcl_feature_zdb_id
    and mrkr_zdb_id ~ 'ZDB-NCCR-190826' and sfcl_assembly is null
and not exists (select * from record_attribution where sfcl_zdb_id = recattrib_data_zdb_id);

select count(*)
from sequence_feature_chromosome_location
         join marker on mrkr_zdb_id = sfcl_feature_zdb_id
    and mrkr_zdb_id ~ 'ZDB-NCCR-190826'
and not exists (select * from record_attribution where sfcl_zdb_id = recattrib_data_zdb_id);

select count(*)
from sequence_feature_chromosome_location
         join marker on mrkr_zdb_id = sfcl_feature_zdb_id
    and mrkr_zdb_id ~ 'ZDB-NCCR-190826' and sfcl_assembly is null;

