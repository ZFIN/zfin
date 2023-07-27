--liquibase formatted sql
--changeset cmpich:ZFIN-8752.sql

-- Report-Genetic-Interactions2
drop table if exists tmp_go;
drop table if exists tmp_go1;
drop table if exists tmp_go4;
drop table if exists tmp_go5;
drop table if exists tmp_go;
drop table if exists finalgo;
drop table if exists finalct3;
drop table if exists finaltemp;
drop table if exists jenk491;
drop table if exists jenk491b;
drop table if exists final491;
drop table if exists tmp_final;

-- Report-Genetic-Interactions
tmp_fish_thatfitthebill;
tmp_fishnoeap;
tmp_onlyfish;
tmp_fish490;
tmp_onlydeficiencies;