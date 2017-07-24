--liquibase formatted sql
--changeset sierra:move_when.sql

alter table updates
 add upd_when datetime year to fraction(3);
