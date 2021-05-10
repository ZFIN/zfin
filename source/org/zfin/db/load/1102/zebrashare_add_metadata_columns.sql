--liquibase formatted sql
--changeset sierra:zebrashare_add_metadata_columns.sql

alter table zebrashare_submission_metadata add column zsm_submitter_name text;

alter table zebrashare_submission_metadata add column zsm_submitter_email text;
