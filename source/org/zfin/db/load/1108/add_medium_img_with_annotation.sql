--liquibase formatted sql
--changeset sierra:add_medium_img_with_annotation.sql

alter table image 
 add img_image_with_annotation_medium text;

update image
 set img_image_with_annotation_medium = replace(img_image_with_annotation, '.', '_medium.');

