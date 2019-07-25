--liquibase formatted sql
--changeset pm:ZFIN-6306

update feature_marker_relationship
set fmrel_type='contains innocuous sequence feature'
where  fmrel_type = 'contains phenotypic sequence feature'
       AND fmrel_mrkr_zdb_id like 'ZDB-GTCONSTRCT%';