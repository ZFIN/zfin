--liquibase formatted sql
--changeset sierra:correspondanceTracking

create table pub_correspondence_sent (pct_pk_id serial8 not null constraint pubct_pk_id_not_null,
       	     				  pct_pub_zdb_id varchar(50) not null constraint pubct_pub_zdb_id,
					  pct_date_entered datetime year to second default current year to second,
					  pct_type int8 not null constraint pubct_type_not_null,	
					  pct_sent_by varchar(50) not null constraint pubct_sent_by_not_null,
					  pct_text lvarchar(10000) not null constraint pubct_text_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;


--create table pub_correspondence_received (pcr_

