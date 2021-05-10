--liquibase formatted sql
--changeset sierra:update_fig_updated_date;

alter table figure
 alter column fig_updated_date set not null;
