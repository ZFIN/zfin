--liquibase formatted sql
--changeset pm:DLOAD-466b


insert into record_attribution (
 recattrib_data_zdb_id,
    recattrib_source_zdb_id
)select  distinct featureid,
        'ZDB-PUB-171006-9'
 from tmp_existfeature;

 insert into record_attribution (
 recattrib_data_zdb_id,
    recattrib_source_zdb_id
)select distinct genoid,
        'ZDB-PUB-171006-9'
 from tmp_existfeature;


 insert into record_attribution (
 recattrib_data_zdb_id,
    recattrib_source_zdb_id
)select  distinct fishid,
        'ZDB-PUB-171006-9'
 from tmp_existfeature;

 insert into record_attribution (
 recattrib_data_zdb_id,
    recattrib_source_zdb_id
)select distinct fmrel,
        'ZDB-PUB-171006-9'
 from tmp_existfeature;

 insert into record_attribution (
 recattrib_data_zdb_id,
    recattrib_source_zdb_id
)select distinct  genofeat,
        'ZDB-PUB-171006-9'
 from tmp_existfeature;

drop table tmp_existfeature;




