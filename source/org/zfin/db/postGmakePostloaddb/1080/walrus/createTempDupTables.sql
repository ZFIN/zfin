--liquibase formatted sql
--changeset sierra:createTempDupTables


create table tmp_load2(expId varchar(50),
			expcondId varchar(50),
			zecoId varchar(50),
			chebiId varchar(50))
in tbldbs2;
