--liquibase formatted sql
--changeset xshao:ZFIN-6021.sql

create temp table tmp_alias_id (
  temp_data_alias_id text
);

insert into tmp_alias_id
select get_id('DALIAS') from single;

insert into zdb_active_data select temp_data_alias_id from tmp_alias_id;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
select temp_data_alias_id, 'ZDB-GENO-990623-2', 'Tuebingen long fin', '1'
  from tmp_alias_id;

delete from tmp_alias_id;

insert into tmp_alias_id
select get_id('DALIAS') from single;

insert into zdb_active_data select temp_data_alias_id from tmp_alias_id;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
select temp_data_alias_id, 'ZDB-GENO-990623-3', 'Tuebingen', '1'
  from tmp_alias_id;

