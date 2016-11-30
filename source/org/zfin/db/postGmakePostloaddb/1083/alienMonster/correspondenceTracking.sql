--liquibase formatted sql
--changeset sierra:correspondanceTracking

create table pub_correspondence_sent_recipient (pubcsr_pk_id serial8 not null constraint pubcsr_pk_id_not_null,
       	     				        pubcsr_recipient_first_name varchar(100),
						pubcsr_recipient_last_name varchar(100),
						pubcsr_recipient_email_address varchar(100),
						pubcsr_pubcs_id int8 not null constraint pubcsr_pubcs_id_not_null,
						pubcsr_recipient_person_zdb_id varchar(50))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;




create table pub_correspondence_sent_text (pubcst_pk_id serial8 not null constraint pubcst_pk_id_not_null,
       	     				   pubcst_date_composed datetime year to second default datetime year to second not null constraint pubcst_date_composed_not_null,
					   pubcst_text_composer varchar(50) not null constraint pubcst_text_composer_not_null,
					   pubcst_text lvarchar(10000) not null constraint pubcst_text_not_null,
					   )
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;


create table pub_correspondence_sent (pubcs_pk_id serial8 not null constraint pubcs_pk_id_not_null,
       	     			     	pubcs_sent_by varchar(50) not null constraint pubcs_sent_by_not_null,
					pubcs_date_sent datetime year to second default datetime year to second not null constraint pubcs_date_sent_not_null,
					pubcs_pubc_text_id int8 not null constraint pubcs_pubc_text_id_not_null,
					pubcs_pub_zdb_id varchar(50) not null constraint pubcs_pub_Zdb_id_not_null,
					pubcs_resend boolean default 'f' not null constraint pubcs_resend_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;


create table pub_correspondence_received (pubcr_pk_id serial8 not null constraint pubcr_pk_id_not_null,
       	     				       pubcr_correspondence_from_first_name varchar(100),
					       pubcr_correspondence_from_last_name varchar(100),
					       pubcr_correspondence_from_email_address varchar(100) 
					       		not null constraint pubcr_correspondence_from_email_address_not_null,
					       pubcr_correspondence_from_person_zdb_id varchar(50),
					       pubcr_pub_zdb_id varchar(50) not null constraint pubcr_pub_zdb_id_not_null,
					       pubcr_received_date datetime year to second default current year to second 
					       	        not null constraint pubcr_received_date_not_null,
					       pubcr_text lvarchar(10000))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;

