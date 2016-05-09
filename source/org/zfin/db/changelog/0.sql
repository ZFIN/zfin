--liquibase formatted sql
--changeset staylor:11
create table test2 (id varchar(50))
in tbldbs2;

--changeset staylor:12
insert into test2 (id) values (1);
insert into test2 (id) values (2);

--changeset staylor:13
create table test (id varchar(50))
in tbldbs2;
