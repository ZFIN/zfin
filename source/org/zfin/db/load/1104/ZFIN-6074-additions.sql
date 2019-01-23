--liquibase formatted sql
--changeset sierra:ZFIN-6074-additions

alter table feature_community_contribution
  alter column fcc_adult_viable drop not null
;

alter table feature_community_contribution
  alter column fcc_maternal_zygosity_examined drop not null
;

alter table feature_community_contribution
 alter column fcc_added_by drop not null
;

alter table feature_community_contribution
  alter column fcc_date_added drop not null
;

alter table faeture_community_contribution
 add constraint fcc_person_fk_odc
 foreign key (fcc_added_by) references person on delete cascade;
