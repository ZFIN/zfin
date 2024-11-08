--liquibase formatted sql
--changeset rtaylor:ZFIN-9427.sql

select convert_marker_type('ZDB-GENEP-160114-76', 'GENE');
