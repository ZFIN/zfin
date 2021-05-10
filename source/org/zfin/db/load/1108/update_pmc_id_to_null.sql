--liquibase formatted sql
--changeset sierra:update_pmc_id_to_null.sql

update publication
set pub_pmc_id = null
 where pub_pmc_id = '';

