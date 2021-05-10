--liquibase formatted sql
--changeset sierra:restore_genox_pre


create table tmp_genox_restore(genox_zdb_id text,
       genox_fish_zdb_id text,
       genox_exp_zdb_id text,
       genox_is_standard boolean,
       genox_is_std_or_generic_control boolean
       );
