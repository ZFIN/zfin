--liquibase formatted sql
--changeset sierra:update_fig_image_date.sql

update figure
 set fig_updated_date = (select to_date(get_date_from_id(fig_zdb_id,'YYYYMMDD'),'YYYYMMDD'));

update figure
 set fig_inserted_date = (select to_date(get_date_from_id(fig_zdb_id,'YYYYMMDD'),'YYYYMMDD'));

update image
 set img_updated_date = (select to_date(get_date_from_id(img_zdb_id,'YYYYMMDD'),'YYYYMMDD'));

update image
 set img_inserted_date = (select to_date(get_date_from_id(img_zdb_id,'YYYYMMDD'),'YYYYMMDD'));

alter table image
 alter column img_inserted_date set not null;

alter table image
 alter column img_updated_date set not null;

alter table figure
 alter column fig_inserted_date set not null;

alter table figure
 alter column fig_inserted_date set not null;



