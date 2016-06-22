--liquibase formatted sql
--changeset sierra:createZecoTempTable

create table tmp_zeco_tt (expid varchar(50),
       	    	  	       cdtid varchar(50),
			       zecoid varchar(50),
			       aoTermId varchar(50),
			       chebiTermId varchar(50),
			       ccTermId varchar(50),
			       otherTermId varchar(50),
			       extra varchar(1))
in tbldbs1
extent size 1024 next size 1024
;
