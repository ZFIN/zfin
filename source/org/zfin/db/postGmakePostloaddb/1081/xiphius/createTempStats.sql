--liquibase formatted sql
--changeset sierra:createTempStats

create table tmp_stats (date datetime year to second,
       	     	       	     section varchar(255),
			     type varchar(255),
			     counter int)
in tbldbs1
extent size 16 next size 16;

