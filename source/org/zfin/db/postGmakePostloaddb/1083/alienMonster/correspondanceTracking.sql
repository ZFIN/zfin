--liquibase formatted sql
--changeset sierra:correspondanceTracking

create table pub_correspondence_sent (pubcs_pk_id serial8 not null constraint pubcs_pk_id_not_null,
       	     			     	pubcs_sent_by varchar(50) not null constraint pubcs_sent_by_not_null,
					pubcs_date_sent datetime year to second default datetime year to second not null constraint pubcs_date_sent_not_null,
					pubcs_pubc_text_id int8 not null constraint pubcs_pubc_text_id_not_null,
					pubcs_pub_zdb_id varchar(50) not null constraint pubcs_pub_Zdb_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;


create table pub_correspondence_text_sent (pubcts_pk_id serial8 not null constraint pubcts_pk_id_not_null,
       	     				   pubcts_date_composed datetime year to second default datetime year to second not null constraint pubcts_date_composed_not_null,
					   pubcts_text lvarchar(10000) not null constraint pubcts_text_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;


create table pub_correspondence_text_received (pubctr_pk_id serial8 not null constraint pubctr_pk_id_not_null,
       	     				       pubctr_correspondence_from_first_name varchar(100),
					       pubctr_correspondence_from_last_name varchar(100),
					       pubctr_correspondence_from_email_address varchar(100) not null constraint pubctr_correspondence_from_email_address_not_null,
					       pubctr_correspondence_from_person_zdb_id varchar(50),
					       pubctr_pub_zdb_id varchar(50) not null constraint pubctr_pub_zdb_id_not_null,
					       pubctr_received_date datetime year to second default current year to second not null constraint
					       			    pubctr_received_date_not_null,
					       pubctr_text lvarchar(10000)


--create table pub_correspondence_received (pcr_

