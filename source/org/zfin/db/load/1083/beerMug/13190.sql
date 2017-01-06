--liquibase formatted sql
--changeset sierra:13190


alter table fish
 add (fish_full_name varchar(30))
;
