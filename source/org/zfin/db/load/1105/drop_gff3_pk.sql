--liquibase formatted sql
--changeset sierra:drop_gff3_pk.sql

alter table gff3 
 drop constraint ngff3_gff_id_primary_key;

