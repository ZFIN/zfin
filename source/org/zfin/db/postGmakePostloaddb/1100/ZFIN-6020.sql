--liquibase formatted sql
--changeset pm:ZFIN-6020

update fish set fish_name=fish_name;
