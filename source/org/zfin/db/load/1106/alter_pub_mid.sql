--liquibase formatted sql
--changeset sierra:alter_pub_mid.sql

alter table publication
 add pub_mid_id text;
