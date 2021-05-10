--liquibase formatted sql
--changeset sierra:updateGenericStd

update fish_experiment
 set genox_exp_zdb_id = genox_exp_zdb_id;

