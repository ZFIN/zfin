--liquibase formatted sql

-- ZFIN-10532: curators could not record a second transcript-consequence row of the
-- same type on one feature (e.g. a second "exon loss" for a different exon).
-- ftmd_altetranscriptte_key_index enforced uniqueness on
-- (ftmd_feature_zdb_id, ftmd_transcript_consequence_term_zdb_id) alone, ignoring the
-- exon/intron number columns that actually distinguish such rows, so the second
-- insert was rejected. Widen it to include exon and intron number.

--changeset rtaylor:ZFIN-10532-feature-transcript-mutation-detail-unique-constraint
ALTER TABLE feature_transcript_mutation_detail
    DROP CONSTRAINT ftmd_altetranscriptte_key_index;

ALTER TABLE feature_transcript_mutation_detail
    ADD CONSTRAINT ftmd_altetranscriptte_key_index
        UNIQUE (ftmd_feature_zdb_id, ftmd_transcript_consequence_term_zdb_id, ftmd_exon_number, ftmd_intron_number);
--rollback ALTER TABLE feature_transcript_mutation_detail DROP CONSTRAINT ftmd_altetranscriptte_key_index;
--rollback ALTER TABLE feature_transcript_mutation_detail ADD CONSTRAINT ftmd_altetranscriptte_key_index UNIQUE (ftmd_feature_zdb_id, ftmd_transcript_consequence_term_zdb_id);
