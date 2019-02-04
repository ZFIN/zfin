--liquibase formatted sql
--changeset sierra:ZFIN-6112.sql

alter table publication
  drop pub_zebrashare_is_public ;
