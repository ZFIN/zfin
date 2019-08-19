--liquibase formatted sql
--changeset sierra:fix_activation.sql

update publication
 set status = 'active'
where exists (select 'x' from to_activate
                     where pub_pmc_id = pmcId)
and status != 'active';;
