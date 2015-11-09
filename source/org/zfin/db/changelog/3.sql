--liquibase formatted sql

--changeset staylor:6
create table test3 (id varchar(50))
in tbldbs2;


--changeset staylor:7
insert into test (id) values (11);
insert into test (id) values (22);
