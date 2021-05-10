--liquibase formatted sql
--changeset sierra:ZFIN-6908.sql

update fish_experiment
  set genox_is_std_or_generic_control = genox_is_std_or_generic_control
  ;
