--liquibase formatted sql
--changeset prita:14985


alter table person
 add person_country varchar(10)
;
