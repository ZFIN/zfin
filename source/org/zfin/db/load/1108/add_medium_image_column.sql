--liquibase formatted sql
--changeset sierra:add_medium_image_column.sql

alter table image 
 add img_medium text;

update image
 set img_medium = replace(img_thumbnail, 'thumb', 'medium');
