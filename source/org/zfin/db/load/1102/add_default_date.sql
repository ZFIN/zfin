--liquibase formatted sql
--changeset sierra:add_default_date.sql

alter table figure
 alter column fig_inserted_date set default current_date;

alter table figure
 alter column fig_updated_date set default current_date;

alter table image
 alter column img_inserted_date set default current_date;

alter table image
 alter column img_updated_date set default current_date;
