--liquibase formatted sql
--changeset cmpich:ZFIN-8205

update zdb_submitters set is_student = 'f'
where zdb_id = 'ZDB-PERS-201217-1';