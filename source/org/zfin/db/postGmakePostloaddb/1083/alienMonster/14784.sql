--liquibase formatted sql
--changeset xiang:14784

delete from foreign_db_contains_display_group_member where fdbcdgm_pk_id in (6, 4, 2, 103);
