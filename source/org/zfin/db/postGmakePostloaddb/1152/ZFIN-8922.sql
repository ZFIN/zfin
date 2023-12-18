--liquibase formatted sql
--changeset cmpich:ZFIN-8922.sql

update zdb_submitters set is_curator = false, is_student = false
where zdb_id in ('ZDB-PERS-990902-1','ZDB-PERS-220425-1','ZDB-PERS-221004-4' );

