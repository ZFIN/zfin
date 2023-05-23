--liquibase formatted sql
--changeset rtaylor:ZFIN-8598-new-feature-marker-rel-type.sql

INSERT INTO feature_marker_relationship_type
    (fmreltype_name, fmreltype_ftr_type_group, fmreltype_mrkr_type_group, fmreltype_1_to_2_comments, fmreltype_2_to_1_comments, fmreltype_produces_affected_marker) 
    VALUES 
    ('mutation involves', 'DEFICIENCY', 'DEFICIENCY_TLOC_MARK', 'mutation involves', 'mutation involved in', 'f');
