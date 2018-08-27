--liquibase formatted sql
--changeset christian:zfin-6011

update feature_transcript_mutation_detail
set ftmd_transcript_consequence_term_zdb_id = 'ZDB-TERM-130401-1577'
where ftmd_transcript_consequence_term_zdb_id = 'ZDB-TERM-130401-1804';

delete from mutation_detail_controlled_vocabulary where
term_ont_id = 'SO:0001816';