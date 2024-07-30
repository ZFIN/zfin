--liquibase formatted sql
--changeset rtaylor:ZFIN-9262.sql

-- Update the display name for the University of Nice Sophia Antipolis to Université Côte d’Azur
UPDATE feature_prefix SET fp_institute_display = 'Université Côte d’Azur' WHERE fp_pk_id = 522;

