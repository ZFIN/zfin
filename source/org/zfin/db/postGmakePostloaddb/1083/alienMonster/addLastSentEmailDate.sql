--liquibase formatted sql
--changeset sierra:addLastSentEmailDate

alter table publication 
 add (pub_last_sent_email_date datetime year to day);

