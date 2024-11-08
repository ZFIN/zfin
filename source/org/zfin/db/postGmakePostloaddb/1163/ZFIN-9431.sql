--liquibase formatted sql
--changeset rtaylor:ZFIN-9431.sql

select convert_marker_type('ZDB-GENE-241029-1', 'LNCRNAG');
select convert_marker_type('ZDB-GENE-241029-2', 'LNCRNAG');
