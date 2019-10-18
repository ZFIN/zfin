--liquibase formatted sql
--changeset pm:ZFIN-6301b.sql



insert into feature_type_ordering (fto_name,fto_priority) values ('MNV',100229);