--liquibase formatted sql
--changeset christian:INF-3020

ALTER TABLE marker_types ALTER COLUMN mrkrtype_type_display TYPE varchar(50) ;

UPDATE marker_types
SET    mrkrtype_type_display = 'Transcriptional Regulatory Region'
WHERE  marker_type = 'TRR';
