--liquibase formatted sql
--changeset pm:DLOAD-466b

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
) values ('ZDB-TGCONSTRCT-170913-6','ZDB-PUB-171006-9');

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
) values ('ZDB-EFG-070117-1','ZDB-PUB-171006-9');

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
) values ('ZDB-EFG-080214-1','ZDB-PUB-171006-9');

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




