--liquibase formatted sql
--changeset rtaylor:ZFIN-8770.sql

ALTER TABLE foreign_db_contains_display_group_member ADD COLUMN can_view boolean DEFAULT true;
ALTER TABLE foreign_db_contains_display_group_member ADD COLUMN can_add boolean DEFAULT true;
ALTER TABLE foreign_db_contains_display_group_member ADD COLUMN can_edit boolean DEFAULT true;
ALTER TABLE foreign_db_contains_display_group_member ADD COLUMN can_delete boolean DEFAULT true;

