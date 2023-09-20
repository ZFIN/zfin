--liquibase formatted sql
--changeset rtaylor:ZFIN-8770.sql

ALTER TABLE foreign_db_contains_display_group_member ADD COLUMN fdbcdgm_can_view boolean DEFAULT true;
ALTER TABLE foreign_db_contains_display_group_member ADD COLUMN fdbcdgm_can_add boolean DEFAULT true;
ALTER TABLE foreign_db_contains_display_group_member ADD COLUMN fdbcdgm_can_edit boolean DEFAULT true;
ALTER TABLE foreign_db_contains_display_group_member ADD COLUMN fdbcdgm_can_delete boolean DEFAULT true;

update foreign_db_contains_display_group_member set fdbcdgm_can_view = true, fdbcdgm_can_add = true, fdbcdgm_can_edit = true, fdbcdgm_can_delete = true;

update foreign_db_contains_display_group_member set fdbcdgm_can_add = false where fdbcdgm_pk_id = 106;
