--liquibase formatted sql
--changeset rtaylor:ZFIN-8598-new-feature-marker-rel-type.sql

-- create new feature_marker_relationship_type
INSERT INTO feature_marker_relationship_type
    (fmreltype_name, fmreltype_ftr_type_group, fmreltype_mrkr_type_group, fmreltype_1_to_2_comments, fmreltype_2_to_1_comments, fmreltype_produces_affected_marker) 
    VALUES 
    ('mutation involves', 'DEFICIENCY', 'DEFICIENCY_TLOC_MARK', 'mutation involves', 'mutation involved in', 't');


-- update existing feature_marker_relationships
UPDATE feature_marker_relationship
SET fmrel_type = 'mutation involves'
WHERE
    fmrel_ftr_zdb_id IN ( SELECT feature_zdb_id FROM feature WHERE feature_type in ( 'DEFICIENCY', 'TRANSLOC', 'INVERSION' ))
    AND fmrel_type = 'is allele of';

