--liquibase formatted sql
--changeset sierra:fix_dalias.sql

create temp table tmp_id (id varchar(50));

insert into tmp_id(id)
 select get_id('DALIAS');

insert into zdb_active_data
 select id from tmp_id;

insert into data_alias (dalias_zdb_id,
       dalias_data_zdb_id,
       dalias_alias,
       dalias_alias_lower,
       dalias_group_id)
select id, 'ZDB-GENE-041229-2','im:7145237sierra','im:714523',1
  from tmp_id;

update marker_history
 set mhist_dalias_zdb_id = (select id from tmp_id)
 where mhist_dalias_zdb_id = 'NOVALUE';

delete from data_alias
 where dalias_zdb_id = 'NOVALUE';

update data_alias
 set dalias_alias = 'im:7145237'
 where dalias_alias = 'im:7145237sierra';
