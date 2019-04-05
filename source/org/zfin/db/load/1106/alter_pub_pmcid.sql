--liquibase formatted sql
--changeset sierra:alter_pub_pmcid.sql

alter table publication
 add pub_pmc_id text;

