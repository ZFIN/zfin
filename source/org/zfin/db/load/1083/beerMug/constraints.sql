--liquibase formatted sql
--changeset sierra:constraints



--pub_correspondence_sent_email

create unique index pub_correspondence_sent_email_pk_index
 on pub_correspondence_sent_email (pubcse_pk_id)
 using btree in idxdbs1;

create unique index pub_correspondence_sent_email_ak_index
 on pub_correspondence_sent_email (pubcse_sent_by, pubcse_subject, pubcse_recipient_group, pubcse_date_composed, pubcse_pub_zdb_id)
 using btree in idxdbs2;

create index pubcse_sent_by_foreign_key_index
 on pub_correspondence_sent_email (pubcse_sent_by)
 using btree in idxdbs3;

create index pubcse_pub_foreign_key_index
 on pub_correspondence_sent_email (pubcse_pub_zdb_id)
 using btree in idxdbs3;


create index pubcse_recipient_group_id_foreign_key_index
 on pub_correspondence_sent_email (pubcse_recipient_Group)
 using btree in idxdbs3;

alter table pub_correspondence_sent_email
  add constraint (foreign key (pubcse_sent_by)
 references person constraint pubcse_sent_by_foreign_key);

alter table pub_correspondence_sent_email
  add constraint (foreign key (pubcse_pub_zdb_id)
 references publication on delete cascade constraint pubcse_pub_zdb_id_foreign_key_odc);


alter table pub_correspondence_sent_email
  add constraint primary key (pubcse_pk_id)
 constraint pub_correspondence_sent_email_pk;

alter table pub_correspondence_sent_email
 add constraint unique (pubcse_sent_by, pubcse_subject, pubcse_recipient_group, pubcse_date_composed, pubcse_pub_Zdb_id)
 constraint pub_correspondence_sent_email_ak;

--pub_correspondence_sent_recipient


create unique index pub_correspondence_recipient_pk_index 
       on pub_correspondence_recipient (pubcr_pk_id)
       using btree in idxdbs2;

create unique index pub_correspondence_recipient_ak_index 
       on pub_correspondence_recipient (pubcr_recipient_email_address, pubcr_recipient_sent_email_id)
       using btree in idxdbs1;

create index pub_correspondence_recipient_person_fk_index
    on pub_correspondence_recipient (pubcr_recipient_person_zdb_id)
       using btree in idxdbs3;

create index pub_correspondence_recipient_group_fk_index
    on pub_correspondence_recipient (pubcr_recipient_sent_email_id)
       using btree in idxdbs3;

alter table pub_correspondence_recipient
  add constraint unique (pubcr_recipient_email_address, pubcr_recipient_sent_email_id)
 constraint pub_corresspondence_recipient_ak;

alter table pub_correspondence_recipient
  add constraint primary key (pubcr_pk_id)
 constraint pub_corresspondence_recipient_pk;

alter table pub_correspondence_recipient
  add constraint (foreign key (pubcr_recipient_person_zdb_id) references person 
 constraint pub_corresspondence_recipient_person_zdb_id_fk);

alter table pub_correspondence_recipient
  add constraint (foreign key (pubcr_recipient_sent_email_id) references  pub_correspondence_sent_email
 constraint pub_corresspondence_recipient_sent_email_id_fk);


--pub_correspondence_subject

create unique index pcs_subject_pk_id_index 
  on pub_correspondence_subject (pcs_subject_pk_id)
 using btree in idxdbs2;

alter table pub_correspondence_subject
 add constraint primary key (pcs_subject_pk_id)
 constraint pub_correspondence_subject_pk;

--pub_correspondence_sent_email_contains_subject

create unique index pubcsecs_pk_index
 on pub_correspondence_sent_email_contains_subject(pubcsecs_sent_email_id,pubcsecs_subject_matter_id)
 using btree in idxdbs3;

alter table pub_correspondence_sent_email_contains_subject
 add constraint primary key (pubcsecs_sent_email_id,pubcsecs_subject_matter_id)
 constraint pubcsecs_pk;


--pub_correspondence_sent_tracker

create unique index pub_correspondence_sent_tracker_pk_index 
       on pub_correspondence_sent_tracker (pubcst_pk_id)
       using btree in idxdbs2;

create unique index pub_correspondence_sent_tracker_ak_index 
       on pub_correspondence_sent_tracker (pubcst_sent_by, pubcst_date_sent, pubcst_sent_email_id, pubcst_pub_zdb_id)
       using btree in idxdbs1;

create index pub_correspondence_sent_tracker_email_id_fk_index
 on  pub_correspondence_sent_tracker (pubcst_sent_email_id)
       using btree in idxdbs2;

create index pub_correspondence_sent_tracker_pub_zdb_id_fk_index
 on pub_correspondence_sent_tracker (pubcst_pub_zdb_id)
       using btree in idxdbs3;

create index pub_correspondence_sent_tracker_sent_by_zdb_id_fk_index
 on  pub_correspondence_sent_tracker (pubcst_sent_by)
       using btree in idxdbs3;

alter table pub_correspondence_sent_tracker
 add constraint primary key (pubcst_pk_id)
 constraint pubcst_sent_tracker_pk;

alter table pub_correspondence_sent_tracker
 add constraint unique (pubcst_sent_by, pubcst_date_sent, pubcst_sent_email_id, pubcst_pub_zdb_id)
 constraint pubcst_sent_tracker_ak;

alter table pub_correspondence_sent_tracker
 add constraint (foreign key (pubcst_sent_by) 
references person constraint pub_correspondence_sent_tracker_sent_by_fk);

alter table pub_correspondence_sent_tracker
 add constraint (foreign key (pubcst_sent_email_id) 
references pub_correspondence_sent_email constraint pub_correspondence_sent_tracker_sent_email_id_fk);

alter table pub_correspondence_sent_tracker
 add constraint (foreign key (pubcst_pub_zdb_id) 
references publication on delete cascade constraint pub_correspondence_sent_tracker_pub_zdb_id_fk_odc);


---TODO: check resend is only true when false exists ---

---TODO: check pub in tracker matches pub in sent email --- 

--pub_correspondence_recieved_email

create unique index pub_correspondence_received_email_pk_index
 on pub_correspondence_received_email (pubcre_pk_id) 
using btree in idxdbs2;

create unique index pub_correspondence_received_email_ak_index
 on pub_correspondence_received_email (pubcre_correspondence_from_email_address, pubcre_subject,pubcre_pub_zdb_id,pubcre_received_date) 
using btree in idxdbs3;

create index pub_correspondence_received_email_pub_fk_index
 on pub_correspondence_received_email (pubcre_pub_zdb_id) 
using btree in idxdbs1;

create index pub_correspondence_received_email_from_person_zdb_id_fk_index
 on pub_correspondence_received_email (pubcre_correspondence_from_person_zdb_id) 
using btree in idxdbs1;

alter table pub_correspondence_received_email
 add constraint primary key (pubcre_pk_id) 
 constraint pubcre_pk;

alter table pub_correspondence_received_email
 add constraint unique (pubcre_correspondence_from_email_address, pubcre_subject,pubcre_pub_zdb_id,pubcre_received_date) 
 constraint pubcre_ak;

alter table pub_correspondence_received_email
 add constraint (foreign key (pubcre_pub_zdb_id) 
references publication on delete cascade constraint pub_correspondence_received_email_pub_fk_odc);

alter table pub_correspondence_received_email
 add constraint (foreign key (pubcre_correspondence_from_person_zdb_id) 
references person constraint pub_correspondence_received_email_from_person_fk);
