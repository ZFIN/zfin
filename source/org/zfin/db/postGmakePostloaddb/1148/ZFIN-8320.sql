--liquibase formatted sql
--changeset cmpich:ZFIN-8320.sql

alter table ui.publication_expression_display add column ped_anatomy_display text;
alter table ui.publication_expression_display add column ped_experiment_display text;
