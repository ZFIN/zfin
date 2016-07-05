--liquibase formatted sql
--changeset sierra:createGap1LoadTable
create table tmp_gap_tt (expcondId varchar(50),
       	     			   expId varchar(50),
				   cdtId varchar(50),
				   zecoId varchar(50),
				   cztId varchar(50),
				   goccId varchar(50))
in tbldbs1
extent size 2048 next size 2048;

