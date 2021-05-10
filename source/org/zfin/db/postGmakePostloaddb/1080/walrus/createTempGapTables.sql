--liquibase formatted sql
--changeset sierra:createTempGapTables


create table tmp_load(expId varchar(50),
			expcondId varchar(50),
			zecoId varchar(50),
			chebiId varchar(50))
in tbldbs2;
