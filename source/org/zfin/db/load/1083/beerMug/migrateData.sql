--liquibase formatted sql
--changeset sierra:migrateData

insert into pub_correspondence_sent_email (pubcse_sent_by, pubcse_text, pubcse_subject, pubcse_date_composed, pubcse_recipient_group, pubcse_pub_zdb_id)
 select distinct pubcorr_curator_zdb_id, "data migration from publication_correspondence", "data migration", pubcorr_contacted_date, "paper author", pubcorr_pub_zdb_id
  from publication_correspondence;


insert into pub_correspondence_received_email (pubcre_correspondence_from_email_address, pubcre_received_date, pubcre_text, pubcre_subject, pubcre_received_by, pubcre_pub_zdb_id) 
 select distinct "data migration",  pubcorr_responded_date, "data migration from publication_correspondence", "data migration", pubcorr_curator_zdb_id, pubcorr_pub_zdb_id
  from publication_correspondence
 where pubcorr_responded_date is not null;

insert into pub_correspondence_sent_tracker (pubcst_sent_by, pubcst_date_sent, pubcst_sent_email_id, pubcst_pub_zdb_id, pubcst_gave_up_date)
  select distinct pubcorr_curator_zdb_id, pubcorr_contacted_date, pubcse_pk_id, pubcorr_pub_zdb_id, pubcorr_gave_up_date
    from publication_correspondence, pub_correspondence_sent_email
 where pubcorr_pub_zdb_id = pubcse_pub_zdb_id
 and pubcorr_gave_up_date is not null;

insert into pub_correspondence_sent_tracker (pubcst_sent_by, pubcst_date_sent, pubcst_sent_email_id, pubcst_pub_zdb_id)
  select distinct pubcorr_curator_zdb_id, pubcorr_contacted_date, pubcse_pk_id, pubcorr_pub_zdb_id
    from publication_correspondence, pub_correspondence_sent_email
 where pubcorr_pub_zdb_id = pubcse_pub_zdb_id
 and pubcorr_gave_up_date is null;
