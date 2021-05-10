
--liquibase formatted sql 
--changeset sierra:rollbackCorrespondence

drop table pub_correspondence_recipient;

drop table pub_correspondence_sent_email_contains_subject;
drop table pub_correspondence_subject;
drop table pub_correspondence_sent_email;
drop table pub_correspondence_sent_tracker;

drop table pub_correspondence_received_email;

drop Table  pub_correspondence_recipient;
drop Table  pub_correspondence_sent_email_contains_subject;

delete from databasechangelog where id in ('correspondenceTracking','constraints');


