--liquibase formatted sql
--changeset cmpich:ZFIN-9155.sql

select count(*)
from ensembl_transcript_renaming;

update marker
set mrkr_abbrev = 'sgms1a-202'
where mrkr_zdb_id = 'ZDB-TSCRIPT-090929-11986';

update marker
set mrkr_abbrev = 'zgc:165582-202',
    mrkr_name   = 'zgc:165582-202'
where mrkr_zdb_id = 'ZDB-TSCRIPT-131113-2029';
