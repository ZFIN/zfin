--liquibase formatted sql
--changeset sierra:restore_genox.sql


delete from tmp_genox_restore
 where genox_zdb_id in (select genox_zdb_id from fish_experiment);

insert into zdb_active_data (zactvd_zdb_id)
 select genox_zdb_id from tmp_genox_restore;

insert into fish_experiment (genox_zdb_id,
       genox_fish_zdb_id,   
       genox_exp_zdb_id,
       genox_is_standard,
       genox_is_std_or_generic_control)
select genox_zdb_id,
       genox_fish_zdb_id,   
       genox_exp_zdb_id,
       genox_is_standard,
       genox_is_std_or_generic_control
 from tmp_genox_restore;

