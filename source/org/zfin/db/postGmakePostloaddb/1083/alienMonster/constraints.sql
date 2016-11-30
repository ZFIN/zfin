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







create unique index pub_correspondence_received_pk_index 
       on pub_correspondence_receievd (pubcsr_pk_id)
       using btree in idxdbs2;

create unique index pub_correspondence_received_ak_index 
       on pub_correspondence_sent_recipient (pubcr_recipient_email_address, pubcr_pubcs_id)
       using btree in idxdbs1;

create index pub_correspondence_sent_recipient_person_fk_index
    on pub_correspondence_sent_recipient (pubcsr_recipient_person_zdb_id)
       using btree in idxdbs3;
