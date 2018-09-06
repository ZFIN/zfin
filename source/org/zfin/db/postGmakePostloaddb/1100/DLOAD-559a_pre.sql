--liquibase formatted sql
--changeset pm:DLOAD-559a_pre

create temp table ftrMutDets (ftr varchar(50), ref1 varchar(50), ref2 varchar(50));

