--liquibase formatted sql
--changeset sierra:constraints

--pub_correspondence_sent_recipient

create unique index pub_correspondence_sent_recipient_pk_index 
       on pub_correspondence_sent_recipient (pubcsr_pk_id)
       using btree in idxdbs2;

create unique index pub_correspondence_sent_recipient_ak_index 
       on pub_correspondence_sent_recipient (pubcsr_recipient_email_address, pubcsr_pubcs_id)
       using btree in idxdbs1;

create index pub_correspondence_sent_recipient_person_fk_index
    on pub_correspondence_sent_recipient (pubcsr_recipient_person_zdb_id)
       using btree in idxdbs3;

alter table pub_correspondence_sent_recipient
  add constraint unique (pubcsr_recipient_email_address, pubcsr_pubcs_id)
 constraint pub_corresspondence_sent_recipient_ak;

alter table pub_correspondence_sent_recipient
  add constraint primary key (pubcsr_pk_id)
 constraint pub_corresspondence_sent_recipient_pk;

alter table pub_correspondence_sent_recipient
  add constraint (foreign key (pubcsr_recipient_person_zdb_id) references person 
 constraint pub_corresspondence_sent_recipient_zdb_id_fk);

--pub_correspondence_sent_text

create unique index pub_correspondence_sent_text_pk_index
 on pub_correspondence_sent_text (pubcst_pk_id)
 using btree in idxdbs1;

alter table pub_correspondence_sent_text
  add constraint primary key (pubcst_pk_id)
 constraint pub_correspondence_sent_text_pk;


--pub_correspondence_sent

create unique index pub_correspondence_sent_pk_index 
       on pub_correspondence_sent (pubcs_pk_id)
       using btree in idxdbs2;

create unique index pub_correspondence_sent_pk_index 
       on pub_correspondence_sent (pubcs_pk_id)
       using btree in idxdbs1;

create unique index pub_correspondence_sent_ak_index 
       on pub_correspondence_sent (pubcs_date_sent, pubcs_pubc_text_id, pubcs_pub_zdb_id, pubcs_resend, pubcs_)
       using btree in idxdbs1;

--can't be a resend if pubc_text_id is not already found in the table with the same pub

create index pub_correspondence_sent_recipient_person_fk_index
    on pub_correspondence_sent_recipient (pubcsr_recipient_person_zdb_id)
       using btree in idxdbs3;
