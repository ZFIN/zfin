--liquibase formatted sql
--changeset pkalita:PLC-317

alter table lab add country varchar(10);
alter table company add country varchar(10);
