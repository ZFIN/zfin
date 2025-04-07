--liquibase formatted sql
--changeset rtaylor:ZFIN-9635

drop table if exists linkage_membership_search_bkup;
drop table if exists sequence_feature_chromosome_location_generated_bkup;

