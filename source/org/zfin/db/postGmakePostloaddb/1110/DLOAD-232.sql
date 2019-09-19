--liquibase formatted sql
--changeset xshao:DLOAD-232

insert into record_attribution(recattrib_data_zdb_id, recattrib_source_zdb_id)
  select distinct dblink_zdb_id, 'ZDB-PUB-020723-5'
    from db_link
   where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37'
     and not exists(select 'x' from record_attribution where recattrib_data_zdb_id = dblink_zdb_id)
     and not exists(select 'x' from expression_experiment2 where xpatex_dblink_zdb_id = dblink_zdb_id);
