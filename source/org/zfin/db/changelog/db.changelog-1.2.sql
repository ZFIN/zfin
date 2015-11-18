
--liquibase formatted sql

--changeset staylor:case13670.0



create table alias1 (id varchar(50),
       	    	  	    data_id varchar(50),
			    alias varchar(50),
			    alias_type int)
fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
  extent size 4096 next size 4096 lock mode row;

