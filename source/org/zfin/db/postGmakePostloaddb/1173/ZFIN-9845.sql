--liquibase formatted sql
--changeset rtaylor:ZFIN-9845

-- Add a uniqueness constraint to marker_annotation_status
alter table marker_annotation_status
    add constraint unique_marker_annotation_status
    unique (mas_mrkr_zdb_id, mas_vt_pk_id);
