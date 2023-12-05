--liquibase formatted sql
--changeset rtaylor:ZFIN-8926.sql

select convert_marker_type('ZDB-LINCRNAG-030131-7204','GENE');

