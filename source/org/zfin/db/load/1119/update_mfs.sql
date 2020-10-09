--liquibase formatted sql
--changeset sierra:update_mfs.sql

alter table mutant_fast_search
  rename column mfs_mrkr_zdb_id to mfs_data_zdb_id;
