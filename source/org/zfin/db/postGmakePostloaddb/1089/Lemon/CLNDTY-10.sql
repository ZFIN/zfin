--liquibase formatted sql
--changeset prita:CLNDTY-10

update feature_marker_relationship set fmrel_type='contains innocuous sequence feature' where fmrel_mrkr_zdb_id like 'ZDB-GTC%' and fmrel_type='contains phenotypic sequence feature';

