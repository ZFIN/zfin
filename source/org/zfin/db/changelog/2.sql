--liquibase formatted sql

--changeset staylor:16
update person
 set name = 'siedawg'
 where name like 'Moxon%';