--liquibase formatted sql

-- ZFIN-10622: complex features support.
--
-- A feature (allele) can now carry more than one variant. New table variant holds one
-- row per variant, with an FK to feature. The four mutation detail tables (genomic, DNA,
-- transcript, protein) get an FK to variant, so their rows belong to a variant
-- instead of directly to the feature.
--
-- Existing data: every feature that has mutation details gets one variant (number 1), and its
-- detail rows are moved onto it. A Complex feature's existing details become its Variant 1.
--
-- The detail tables' own *_feature_zdb_id columns are dropped at the end: the feature is reached
-- through the variant (detail -> variant -> feature). Nothing in the database (views,
-- functions, triggers) reads them; the Hibernate mappings and SQL scripts that do must join
-- through variant before this is applied.


-- ─── variant ────────────────────────────────────────────────────
--changeset cmpich:ZFIN-10622-create-variant
-- var_variant_number: display order within the feature (Variant 1, 2, 3 ...).
-- var_variant_type: the single-variant type (Point Mutation, Small Deletion, ...), from
--   feature_type. NULL when not yet curated, e.g. the migrated Variant 1 of a Complex feature.
CREATE TABLE variant (
    var_zdb_id          text      NOT NULL,
    var_feature_zdb_id  text      NOT NULL,
    var_variant_number  integer   NOT NULL DEFAULT 1,
    var_variant_type    text,
    var_modified_at     timestamp NOT NULL DEFAULT now(),
    CONSTRAINT variant_pkey PRIMARY KEY (var_zdb_id),
    CONSTRAINT var_zdb_id_fk_odc FOREIGN KEY (var_zdb_id)
        REFERENCES zdb_active_data (zactvd_zdb_id) ON DELETE CASCADE,
    CONSTRAINT var_feature_fk_odc FOREIGN KEY (var_feature_zdb_id)
        REFERENCES feature (feature_zdb_id) ON DELETE CASCADE,
    CONSTRAINT var_variant_type_fk FOREIGN KEY (var_variant_type)
        REFERENCES feature_type (ftrtype_name) ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT var_variant_number_positive CHECK (var_variant_number > 0),
    CONSTRAINT var_feature_variant_number_ak UNIQUE (var_feature_zdb_id, var_variant_number)
);
--rollback DROP TABLE variant;


-- ─── ZDB ID wiring for VAR ─────────────────────────────────────────────
-- Must follow CREATE TABLE: a trigger on zdb_object_type checks that the home table exists.
-- get_id('VAR') needs a zdb_object_type row and a var_seq sequence. The Java side
-- (ActiveData.Type.VAR + the entity's ZdbIdGenerator parameter) comes with the Hibernate
-- changes.
--changeset cmpich:ZFIN-10622-var-id-wiring runOnChange:true
INSERT INTO zdb_object_type (
    zobjtype_name, zobjtype_day, zobjtype_home_schema, zobjtype_home_table,
    zobjtype_home_zdb_id_column, zobjtype_is_data, zobjtype_is_source)
VALUES ('VAR', current_date - 1, 'public', 'variant',
        'var_zdb_id', true, false)
ON CONFLICT (zobjtype_name) DO UPDATE SET
    zobjtype_home_schema = EXCLUDED.zobjtype_home_schema,
    zobjtype_home_table = EXCLUDED.zobjtype_home_table,
    zobjtype_home_zdb_id_column = EXCLUDED.zobjtype_home_zdb_id_column;

CREATE SEQUENCE IF NOT EXISTS var_seq START 1;
--rollback DROP SEQUENCE IF EXISTS var_seq;
--rollback DELETE FROM zdb_object_type WHERE zobjtype_name = 'VAR';


-- ─── variant FK columns on the mutation detail tables ───────────────────
--changeset cmpich:ZFIN-10622-detail-variant-columns
ALTER TABLE feature_genomic_mutation_detail    ADD COLUMN fgmd_variant_zdb_id text;
ALTER TABLE feature_dna_mutation_detail        ADD COLUMN fdmd_variant_zdb_id text;
ALTER TABLE feature_transcript_mutation_detail ADD COLUMN ftmd_variant_zdb_id text;
ALTER TABLE feature_protein_mutation_detail    ADD COLUMN fpmd_variant_zdb_id text;
--rollback ALTER TABLE feature_genomic_mutation_detail    DROP COLUMN fgmd_variant_zdb_id;
--rollback ALTER TABLE feature_dna_mutation_detail        DROP COLUMN fdmd_variant_zdb_id;
--rollback ALTER TABLE feature_transcript_mutation_detail DROP COLUMN ftmd_variant_zdb_id;
--rollback ALTER TABLE feature_protein_mutation_detail    DROP COLUMN fpmd_variant_zdb_id;


-- ─── migrate existing details onto Variant 1 ────────────────────────────
--changeset cmpich:ZFIN-10622-migrate-existing-variants
-- One variant per feature that has any mutation detail or a flanking sequence. Its type is the
-- feature's own type, except for Complex features: their single existing variant has no known
-- type yet.
INSERT INTO variant (var_zdb_id, var_feature_zdb_id, var_variant_number, var_variant_type)
SELECT get_id_and_insert_active_data('VAR'),
       f.feature_zdb_id,
       1,
       CASE WHEN f.feature_type = 'COMPLEX_SUBSTITUTION' THEN NULL ELSE f.feature_type END
  FROM feature f
 WHERE EXISTS (SELECT 1 FROM feature_genomic_mutation_detail    WHERE fgmd_feature_zdb_id = f.feature_zdb_id)
    OR EXISTS (SELECT 1 FROM feature_dna_mutation_detail        WHERE fdmd_feature_zdb_id = f.feature_zdb_id)
    OR EXISTS (SELECT 1 FROM feature_transcript_mutation_detail WHERE ftmd_feature_zdb_id = f.feature_zdb_id)
    OR EXISTS (SELECT 1 FROM feature_protein_mutation_detail    WHERE fpmd_feature_zdb_id = f.feature_zdb_id)
    OR EXISTS (SELECT 1 FROM variant_flanking_sequence          WHERE vfseq_data_zdb_id   = f.feature_zdb_id)
 ORDER BY f.feature_zdb_id;

UPDATE feature_genomic_mutation_detail d
   SET fgmd_variant_zdb_id = v.var_zdb_id
  FROM variant v
 WHERE v.var_feature_zdb_id = d.fgmd_feature_zdb_id AND v.var_variant_number = 1;

UPDATE feature_dna_mutation_detail d
   SET fdmd_variant_zdb_id = v.var_zdb_id
  FROM variant v
 WHERE v.var_feature_zdb_id = d.fdmd_feature_zdb_id AND v.var_variant_number = 1;

UPDATE feature_transcript_mutation_detail d
   SET ftmd_variant_zdb_id = v.var_zdb_id
  FROM variant v
 WHERE v.var_feature_zdb_id = d.ftmd_feature_zdb_id AND v.var_variant_number = 1;

UPDATE feature_protein_mutation_detail d
   SET fpmd_variant_zdb_id = v.var_zdb_id
  FROM variant v
 WHERE v.var_feature_zdb_id = d.fpmd_feature_zdb_id AND v.var_variant_number = 1;
--rollback UPDATE feature_genomic_mutation_detail    SET fgmd_variant_zdb_id = NULL;
--rollback UPDATE feature_dna_mutation_detail        SET fdmd_variant_zdb_id = NULL;
--rollback UPDATE feature_transcript_mutation_detail SET ftmd_variant_zdb_id = NULL;
--rollback UPDATE feature_protein_mutation_detail    SET fpmd_variant_zdb_id = NULL;
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT var_zdb_id FROM variant);




-- ─── constraints: details belong to a variant ───────────────────────────
--changeset cmpich:ZFIN-10622-detail-variant-constraints
-- Every detail row has a variant. Uniqueness moves from per feature to per variant: a Complex
-- feature has one genomic, DNA and protein detail row per variant.
ALTER TABLE feature_genomic_mutation_detail
    ALTER COLUMN fgmd_variant_zdb_id SET NOT NULL,
    ADD CONSTRAINT fgmd_variant_fk_odc FOREIGN KEY (fgmd_variant_zdb_id)
        REFERENCES variant (var_zdb_id) ON DELETE CASCADE,
    ADD CONSTRAINT fgmd_variant_ak UNIQUE (fgmd_variant_zdb_id);

ALTER TABLE feature_dna_mutation_detail
    ALTER COLUMN fdmd_variant_zdb_id SET NOT NULL,
    ADD CONSTRAINT fdmd_variant_fk_odc FOREIGN KEY (fdmd_variant_zdb_id)
        REFERENCES variant (var_zdb_id) ON DELETE CASCADE,
    ADD CONSTRAINT fdmd_variant_ak UNIQUE (fdmd_variant_zdb_id);

ALTER TABLE feature_transcript_mutation_detail
    ALTER COLUMN ftmd_variant_zdb_id SET NOT NULL,
    ADD CONSTRAINT ftmd_variant_fk_odc FOREIGN KEY (ftmd_variant_zdb_id)
        REFERENCES variant (var_zdb_id) ON DELETE CASCADE,
    ADD CONSTRAINT ftmd_variant_transcript_ak
        UNIQUE (ftmd_variant_zdb_id, ftmd_transcript_consequence_term_zdb_id, ftmd_exon_number, ftmd_intron_number);

ALTER TABLE feature_protein_mutation_detail
    ALTER COLUMN fpmd_variant_zdb_id SET NOT NULL,
    ADD CONSTRAINT fpmd_variant_fk_odc FOREIGN KEY (fpmd_variant_zdb_id)
        REFERENCES variant (var_zdb_id) ON DELETE CASCADE,
    ADD CONSTRAINT fpmd_variant_ak UNIQUE (fpmd_variant_zdb_id);
--rollback ALTER TABLE feature_protein_mutation_detail DROP CONSTRAINT fpmd_variant_ak, DROP CONSTRAINT fpmd_variant_fk_odc, ALTER COLUMN fpmd_variant_zdb_id DROP NOT NULL;
--rollback ALTER TABLE feature_transcript_mutation_detail DROP CONSTRAINT ftmd_variant_transcript_ak, DROP CONSTRAINT ftmd_variant_fk_odc, ALTER COLUMN ftmd_variant_zdb_id DROP NOT NULL;
--rollback ALTER TABLE feature_dna_mutation_detail DROP CONSTRAINT fdmd_variant_ak, DROP CONSTRAINT fdmd_variant_fk_odc, ALTER COLUMN fdmd_variant_zdb_id DROP NOT NULL;
--rollback ALTER TABLE feature_genomic_mutation_detail DROP CONSTRAINT fgmd_variant_ak, DROP CONSTRAINT fgmd_variant_fk_odc, ALTER COLUMN fgmd_variant_zdb_id DROP NOT NULL;


-- ─── drop the detail tables' feature columns ────────────────────────────
--changeset cmpich:ZFIN-10622-drop-detail-feature-columns
-- The feature is now reached through the variant. DROP COLUMN also drops the old FKs to
-- feature and the per-feature unique constraints (fgmd_ak_constraint,
-- feature_dna_mutation_detail_alternate_key_index, ftmd_altetranscriptte_key_index,
-- fpmd_feature_zdb_id_alternate_key_index).
ALTER TABLE feature_genomic_mutation_detail    DROP COLUMN fgmd_feature_zdb_id;
ALTER TABLE feature_dna_mutation_detail        DROP COLUMN fdmd_feature_zdb_id;
ALTER TABLE feature_transcript_mutation_detail DROP COLUMN ftmd_feature_zdb_id;
ALTER TABLE feature_protein_mutation_detail    DROP COLUMN fpmd_feature_zdb_id;
--rollback ALTER TABLE feature_genomic_mutation_detail ADD COLUMN fgmd_feature_zdb_id text;
--rollback UPDATE feature_genomic_mutation_detail d SET fgmd_feature_zdb_id = v.var_feature_zdb_id FROM variant v WHERE v.var_zdb_id = d.fgmd_variant_zdb_id;
--rollback ALTER TABLE feature_genomic_mutation_detail ALTER COLUMN fgmd_feature_zdb_id SET NOT NULL, ADD CONSTRAINT fgmd_feature_fk_odc FOREIGN KEY (fgmd_feature_zdb_id) REFERENCES feature (feature_zdb_id) ON DELETE CASCADE, ADD CONSTRAINT fgmd_ak_constraint UNIQUE (fgmd_feature_zdb_id);
--rollback ALTER TABLE feature_dna_mutation_detail ADD COLUMN fdmd_feature_zdb_id text;
--rollback UPDATE feature_dna_mutation_detail d SET fdmd_feature_zdb_id = v.var_feature_zdb_id FROM variant v WHERE v.var_zdb_id = d.fdmd_variant_zdb_id;
--rollback ALTER TABLE feature_dna_mutation_detail ALTER COLUMN fdmd_feature_zdb_id SET NOT NULL, ADD CONSTRAINT fdmd_feature_zdb_id_foreign_key FOREIGN KEY (fdmd_feature_zdb_id) REFERENCES feature (feature_zdb_id) ON UPDATE RESTRICT ON DELETE RESTRICT, ADD CONSTRAINT feature_dna_mutation_detail_alternate_key_index UNIQUE (fdmd_feature_zdb_id);
--rollback ALTER TABLE feature_transcript_mutation_detail ADD COLUMN ftmd_feature_zdb_id text;
--rollback UPDATE feature_transcript_mutation_detail d SET ftmd_feature_zdb_id = v.var_feature_zdb_id FROM variant v WHERE v.var_zdb_id = d.ftmd_variant_zdb_id;
--rollback ALTER TABLE feature_transcript_mutation_detail ALTER COLUMN ftmd_feature_zdb_id SET NOT NULL, ADD CONSTRAINT ftmd_feature_zdb_id_fk_odc FOREIGN KEY (ftmd_feature_zdb_id) REFERENCES feature (feature_zdb_id) ON UPDATE RESTRICT ON DELETE CASCADE, ADD CONSTRAINT ftmd_altetranscriptte_key_index UNIQUE (ftmd_feature_zdb_id, ftmd_transcript_consequence_term_zdb_id, ftmd_exon_number, ftmd_intron_number);
--rollback ALTER TABLE feature_protein_mutation_detail ADD COLUMN fpmd_feature_zdb_id text;
--rollback UPDATE feature_protein_mutation_detail d SET fpmd_feature_zdb_id = v.var_feature_zdb_id FROM variant v WHERE v.var_zdb_id = d.fpmd_variant_zdb_id;
--rollback ALTER TABLE feature_protein_mutation_detail ALTER COLUMN fpmd_feature_zdb_id SET NOT NULL, ADD CONSTRAINT fpmd_feature_zdb_id_fk_constraint FOREIGN KEY (fpmd_feature_zdb_id) REFERENCES feature (feature_zdb_id) ON UPDATE RESTRICT ON DELETE RESTRICT, ADD CONSTRAINT fpmd_feature_zdb_id_alternate_key_index UNIQUE (fpmd_feature_zdb_id);


-- ─── flanking sequence belongs to a variant ─────────────────────────────
--changeset cmpich:ZFIN-10622-flanking-sequence-variant
-- The flanking sequence is computed around one variant's mutation, so it points at that
-- variant. Existing rows go to Variant 1 of their feature (created above for every feature
-- with a flanking sequence). vfseq_data_zdb_id stays: it still names the feature.
ALTER TABLE variant_flanking_sequence ADD COLUMN vfseq_variant_zdb_id text;

UPDATE variant_flanking_sequence s
   SET vfseq_variant_zdb_id = v.var_zdb_id
  FROM variant v
 WHERE v.var_feature_zdb_id = s.vfseq_data_zdb_id AND v.var_variant_number = 1;

ALTER TABLE variant_flanking_sequence
    ALTER COLUMN vfseq_variant_zdb_id SET NOT NULL,
    ADD CONSTRAINT vfseq_variant_fk_odc FOREIGN KEY (vfseq_variant_zdb_id)
        REFERENCES variant (var_zdb_id) ON DELETE CASCADE,
    ADD CONSTRAINT vfseq_variant_ak UNIQUE (vfseq_variant_zdb_id, vfseq_flanking_sequence_type);
--rollback ALTER TABLE variant_flanking_sequence DROP COLUMN vfseq_variant_zdb_id;


-- ─── drop the flanking sequence's feature column ────────────────────────
--changeset cmpich:ZFIN-10622-drop-flanking-sequence-feature-column
-- The feature is now reached through the variant. DROP COLUMN also drops the old FK to
-- zdb_active_data (variant_sequence_zdb_active_data_foreign_key) and the per-feature unique key
-- (vfseq_alternate_key); deleting a feature still removes its flanking sequence, through the
-- variant (var_feature_fk_odc, vfseq_variant_fk_odc).
ALTER TABLE variant_flanking_sequence DROP COLUMN vfseq_data_zdb_id;
--rollback ALTER TABLE variant_flanking_sequence ADD COLUMN vfseq_data_zdb_id text;
--rollback UPDATE variant_flanking_sequence s SET vfseq_data_zdb_id = v.var_feature_zdb_id FROM variant v WHERE v.var_zdb_id = s.vfseq_variant_zdb_id;
--rollback ALTER TABLE variant_flanking_sequence ALTER COLUMN vfseq_data_zdb_id SET NOT NULL, ADD CONSTRAINT variant_sequence_zdb_active_data_foreign_key FOREIGN KEY (vfseq_data_zdb_id) REFERENCES zdb_active_data (zactvd_zdb_id) ON DELETE CASCADE, ADD CONSTRAINT vfseq_alternate_key UNIQUE (vfseq_data_zdb_id, vfseq_flanking_sequence_type);
