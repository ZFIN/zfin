--liquibase formatted sql
--changeset cmpich:ZFIN-9850-update-omc-institution

UPDATE feature_prefix
SET fp_institute_display = 'Osaka Medical and Pharmaceutical University'
WHERE fp_prefix = 'omc';
