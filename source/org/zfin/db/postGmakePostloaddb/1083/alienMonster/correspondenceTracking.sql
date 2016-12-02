--liquibase formatted sql
--changeset sierra:correspondenceTracking

create table pub_correspondence_recipient (pubcr_pk_id serial8 not null constraint pubcr_pk_id_not_null,
       	     				        pubcr_recipient_first_name varchar(100),
						pubcr_recipient_last_name varchar(100),
						pubcr_recipient_email_address varchar(100),
						pubcr_recipient_person_zdb_id varchar(50),
						pubcr_recipient_sent_email_id int8 not null constraint pubcr_recipient_sent_email_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;


create table pub_correspondence_sent_email (pubcse_pk_id serial8 not null constraint pubcse_pk_id_not_null,
       	     				   pubcse_date_composed datetime year to day default current year to day not null constraint pubcse_date_composed_not_null,
					   pubcse_sent_by varchar(50) not null constraint pubcse_sent_by_not_null,
					   pubcse_text lvarchar(10000) not null constraint pubcse_text_not_null,
					   pubcse_subject varchar(100) not null constraint pubcse_subject_not_null,
					   pubcse_recipient_group varchar(100) not null constraint pubcse_recipient_group_not_null,
					   pubcse_pub_zdb_id varchar(50) not null constraint pubcse_pub_zdb_id_not_null
					   )
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;


create table pub_correspondence_sent_email_contains_subject (pubcsecs_sent_email_id int8 not null constraint pubcsecs_sent_email_id_not_null,
       	     						     pubcsecs_subject_matter_id int8 not null constraint pubcsecs_subject_matter_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;


create table pub_correspondence_subject (pcs_subject_pk_id serial8 not null constraint pcs_subject_pk_id_not_null,
       	     				 pcs_subject_text lvarchar(1000) not null constraint pcs_subject_text_not_null,
					 pcs_subject_type varchar(20) not null constraint pcs_subject_type_not_null,
					 pcs_handle varchar(100) not null constraint pcs_handle_not_null)
in tbldbs2
extent size 32 next size 32;



create table pub_correspondence_sent_tracker (pubcst_pk_id serial8 not null constraint pubcst_pk_id_not_null,
       	     			     	pubcst_sent_by varchar(50) not null constraint pubcst_sent_by_not_null,
					pubcst_date_sent datetime year to day default current year to day not null constraint pubcst_date_sent_not_null,
					pubcst_sent_email_id int8 not null constraint pubcst_sent_email_id_not_null,
					pubcst_pub_zdb_id varchar(50) not null constraint pubcst_pub_Zdb_id_not_null,
					pubcst_resend boolean default 'f' not null constraint pubcst_resend_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;


create table pub_correspondence_received_email (pubcre_pk_id serial8 not null constraint pubcre_pk_id_not_null,
       	     				       pubcre_correspondence_from_first_name varchar(100),
					       pubcre_correspondence_from_last_name varchar(100),
					       pubcre_correspondence_from_email_address varchar(100) 
					       		not null constraint pubcre_correspondence_from_email_address_not_null,
					       pubcre_correspondence_from_person_zdb_id varchar(50),
					       pubcre_pub_zdb_id varchar(50) not null constraint pubcre_pub_zdb_id_not_null,
					       pubcre_received_date datetime year to second default current year to second 
					       	        not null constraint pubcre_received_date_not_null,
					       pubcre_text lvarchar(10000),
					       pubcre_subject varchar(100) not null constraint pubcre_subject_not_null,
					       pubcre_received_by varchar(50))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;


