--liquibase formatted sql
--changeset rtaylor:ZFIN-8822.sql

select convert_marker_type('ZDB-LINCRNAG-030804-19','GENE');

