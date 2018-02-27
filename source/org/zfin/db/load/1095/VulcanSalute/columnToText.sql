--liquibase formatted sql
--changeset sierra:columnToText.sql

alter table marker_go_term_annotation_extension_group
 drop mgtaeg_logical_operator;

alter table marker_go_term_annotation_extension
  modify (mgtae_identifier_term_zdb_id varchar(50));

alter table marker_go_term_annotation_extension
  add (mgtae_term_text varchar(255));

alter table marker_go_term_annotation_extension
 add (mgtae_dblink_zdb_id varchar(50));

