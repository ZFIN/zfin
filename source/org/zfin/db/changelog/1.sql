--liquibase formatted sql

--changeset staylor:14
create table test4 (id varchar(50))
in tbldbs2;


--changeset staylor:15
insert into test (id) values (1);
insert into test (id) values (2);
