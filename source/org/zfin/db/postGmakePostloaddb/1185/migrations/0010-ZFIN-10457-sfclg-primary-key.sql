--liquibase formatted sql
--changeset rtaylor:ZFIN-10457-sfclg-primary-key

-- sfclg_pk_id is already a de facto primary key -- NOT NULL, sequence default, no duplicates -- it
-- was just never declared as one. Making it official fixes marker merges, which need a primary key
-- to resolve unique-constraint conflicts on this table. See ZFIN-10457.

ALTER TABLE sequence_feature_chromosome_location_generated
    ADD CONSTRAINT sequence_feature_chromosome_location_generated_pkey PRIMARY KEY (sfclg_pk_id);
