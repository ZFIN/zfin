--liquibase formatted sql
--changeset sierra:ZFIN-6074-datetime

alter table feature_community_contribution
  alter column fcc_date_added set data type timestamp;

alter table feature_community_contribution
  alter column fcc_date_added set not null;

alter table feature_community_contribution
  alter column fcc_added_by  set not null;

