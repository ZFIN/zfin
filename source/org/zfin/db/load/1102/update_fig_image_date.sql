--liquibase formatted sql
--changeset sierra:update_fig_image_date.sql

update figure
 set fig_updated_date = select get_date_from_id(fig_zdb_id,'MM/DD/YYYY');

update figure
 set fig_inserted_date = select get_date_from_id(fig_zdb_id,'MM/DD/YYYY');

update image
 set img_updated_date = select get_date_from_id(img_zdb_id,'MM/DD/YYYY');

update image
 set img_inserted_date = select get_date_from_id(img_zdb_id,'MM/DD/YYYY');

alter table image
 alter column img_inserted_date set not null;

alter table image
 alter column img_updated_date set not null;

alter table figure
 alter column fig_inserted_date set not null;

alter table figure
 alter column fig_inserted_date set not null;



