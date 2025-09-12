--liquibase formatted sql
--changeset rtaylor:ZFIN-9845

-- Remove any duplicates:
DELETE FROM marker_annotation_status
WHERE ctid NOT IN (
    SELECT ctid
    FROM (
             SELECT ctid,
                    ROW_NUMBER() OVER (PARTITION BY mas_mrkr_zdb_id, mas_vt_pk_id ORDER BY ctid) as rn
             FROM marker_annotation_status
         ) t
    WHERE rn = 1
);

-- Add a uniqueness constraint to marker_annotation_status
alter table marker_annotation_status
    add constraint unique_marker_annotation_status
    unique (mas_mrkr_zdb_id);