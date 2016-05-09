--liquibase formatted sql
--changeset staylor:6

create temp table tmp_test (id int8);
insert into tmp_test (id)
 select 1 from single;

--changeset staylor:7

create temp table tmp_test2 (id varchar(50))
with no log;

insert into tmp_test2 (id)
 select 555 from single;

insert into zdb_Active_data
 select id from tmp_test2;

select * from zdb_active_Data
 where zactvd_zdb_id = '555';

--changeset staylor:8

delete from zdb_active_data
 where zactvd_zdb_id = '555';
