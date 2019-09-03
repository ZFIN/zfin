--liquibase formatted sql
--changeset xshao:DLOAD-232

insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id)
  select id, 'ZDB-PUB-020723-5'
  from dblink;

