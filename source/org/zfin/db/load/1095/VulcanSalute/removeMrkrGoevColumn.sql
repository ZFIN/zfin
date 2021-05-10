--liquibase formatted sql
--changeset sierra:removeMrkrGoevColumn.sql


alter table marker_go_term_annotation_extension
 drop mgtae_mrkrgoev_zdb_id;
