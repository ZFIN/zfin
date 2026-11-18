--liquibase formatted sql

-- ZFIN-10622: complex features, first curated examples.
--
-- Source for both: ZDB-PUB-220901-7 (Sci Adv 2022, doi:10.1126/sciadv.abn2082), Fig. S11. Bases are
-- given on the GRCz12tu + strand. Each deletion is placed as in the figure, which is its 3'-most
-- position on the transcript.
--
-- bns497 (ZDB-ALT-230227-6, Complex, tal1), chr22. The figure's wild-type sequence matches the
-- + strand from 16249157.
--   Variant 1: Small Deletion  16249186-16249192  GTCTGGC > -   (equivalent: 16249185-16249191, 16249184-16249190)
--   Variant 2: Point Mutation  16249209           T > C
--
-- bns426 (ZDB-ALT-230227-4, Complex, etsrp), chr16. etsrp is on the - strand; the figure's
-- wild-type sequence matches the - strand over 46433927-46433984.
--   Variant 1: Small Deletion  46433934-46433942  TCCCAGCCA > -  (transcript TGGCTGGGA; in frame, loses D265 G266 W267;
--                                                                 equivalent: 46433935-46433943, 46433936-46433944)
--   Variant 2: Point Mutation  46433969           G > A          (transcript C > T; silent, Cys256 TGC > TGT)
--
-- A chromosome location can now name the variant it locates (sfcl_variant_zdb_id), so each
-- variant of a Complex feature has its own position. The column is nullable: existing
-- locations stay feature-level.


-- ─── chromosome location can belong to a variant ────────────────────────
--changeset cmpich:ZFIN-10622-sfcl-variant-column
ALTER TABLE sequence_feature_chromosome_location
    ADD COLUMN sfcl_variant_zdb_id text,
    ADD CONSTRAINT sfcl_variant_fk_odc FOREIGN KEY (sfcl_variant_zdb_id)
        REFERENCES variant (var_zdb_id) ON DELETE CASCADE;

CREATE INDEX sfcl_variant_fk_index ON sequence_feature_chromosome_location (sfcl_variant_zdb_id);
--rollback DROP INDEX IF EXISTS sfcl_variant_fk_index;
--rollback ALTER TABLE sequence_feature_chromosome_location DROP COLUMN sfcl_variant_zdb_id;


-- ─── bns497: two variants with their GRCz12tu locations and base changes ─
--changeset cmpich:ZFIN-10622-bns497-variants
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:1 SELECT count(*) FROM feature WHERE feature_zdb_id = 'ZDB-ALT-230227-6' AND feature_abbrev = 'bns497'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-230227-6'
-- Locations: evidence ECO:0000304 (TAS). Locations and genomic mutation details are attributed to
-- the feature's publication ZDB-PUB-220901-7. A deletion's sequence of variation is NULL, as for
-- the other deletions.
WITH new_variant AS (
    INSERT INTO variant (var_zdb_id, var_feature_zdb_id, var_variant_number, var_variant_type)
    VALUES (get_id_and_insert_active_data('VAR'), 'ZDB-ALT-230227-6', 1, 'DELETION'),
           (get_id_and_insert_active_data('VAR'), 'ZDB-ALT-230227-6', 2, 'POINT_MUTATION')
    RETURNING var_zdb_id, var_feature_zdb_id, var_variant_number
), new_genomic_detail AS (
    INSERT INTO feature_genomic_mutation_detail (
        fgmd_zdb_id, fgmd_variant_zdb_id,
        fgmd_sequence_of_reference, fgmd_sequence_of_variation, fgmd_variation_strand)
    SELECT get_id_and_insert_active_data('FGMD'), v.var_zdb_id,
           seq.reference, seq.variation, '+'
      FROM new_variant v
      JOIN (VALUES (1, 'GTCTGGC', NULL),
                   (2, 'T',       'C')) AS seq (variant_number, reference, variation)
        ON seq.variant_number = v.var_variant_number
    RETURNING fgmd_zdb_id
), new_location AS (
    INSERT INTO sequence_feature_chromosome_location (
        sfcl_zdb_id, sfcl_feature_zdb_id, sfcl_variant_zdb_id,
        sfcl_start_position, sfcl_end_position, sfcl_assembly, sfcl_chromosome,
        sfcl_chromosome_reference_accession_number, sfcl_evidence_code)
    SELECT get_id_and_insert_active_data('SFCL'), v.var_feature_zdb_id, v.var_zdb_id,
           pos.start_position, pos.end_position, 'GRCz12tu', '22',
           'NC_133197.1', 'ZDB-TERM-170419-250'
      FROM new_variant v
      JOIN (VALUES (1, 16249186, 16249192),
                   (2, 16249209, 16249209)) AS pos (variant_number, start_position, end_position)
        ON pos.variant_number = v.var_variant_number
    RETURNING sfcl_zdb_id
)
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
SELECT sfcl_zdb_id, 'ZDB-PUB-220901-7' FROM new_location
UNION ALL
SELECT fgmd_zdb_id, 'ZDB-PUB-220901-7' FROM new_genomic_detail;
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT s.sfcl_zdb_id FROM sequence_feature_chromosome_location s JOIN variant v ON v.var_zdb_id = s.sfcl_variant_zdb_id WHERE v.var_feature_zdb_id = 'ZDB-ALT-230227-6');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT g.fgmd_zdb_id FROM feature_genomic_mutation_detail g JOIN variant v ON v.var_zdb_id = g.fgmd_variant_zdb_id WHERE v.var_feature_zdb_id = 'ZDB-ALT-230227-6');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT var_zdb_id FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-230227-6');


-- ─── bns426: two variants with their GRCz12tu locations and base changes ─
--changeset cmpich:ZFIN-10622-bns426-variants
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:1 SELECT count(*) FROM feature WHERE feature_zdb_id = 'ZDB-ALT-230227-4' AND feature_abbrev = 'bns426'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-230227-4'
-- Same conventions as bns497: evidence ECO:0000304 (TAS), attributed to ZDB-PUB-220901-7, + strand
-- bases, deletion's sequence of variation NULL. The feature's existing GRCz11 chr16 location (no
-- positions) is left as it is.
WITH new_variant AS (
    INSERT INTO variant (var_zdb_id, var_feature_zdb_id, var_variant_number, var_variant_type)
    VALUES (get_id_and_insert_active_data('VAR'), 'ZDB-ALT-230227-4', 1, 'DELETION'),
           (get_id_and_insert_active_data('VAR'), 'ZDB-ALT-230227-4', 2, 'POINT_MUTATION')
    RETURNING var_zdb_id, var_feature_zdb_id, var_variant_number
), new_genomic_detail AS (
    INSERT INTO feature_genomic_mutation_detail (
        fgmd_zdb_id, fgmd_variant_zdb_id,
        fgmd_sequence_of_reference, fgmd_sequence_of_variation, fgmd_variation_strand)
    SELECT get_id_and_insert_active_data('FGMD'), v.var_zdb_id,
           seq.reference, seq.variation, '+'
      FROM new_variant v
      JOIN (VALUES (1, 'TCCCAGCCA', NULL),
                   (2, 'G',         'A')) AS seq (variant_number, reference, variation)
        ON seq.variant_number = v.var_variant_number
    RETURNING fgmd_zdb_id
), new_location AS (
    INSERT INTO sequence_feature_chromosome_location (
        sfcl_zdb_id, sfcl_feature_zdb_id, sfcl_variant_zdb_id,
        sfcl_start_position, sfcl_end_position, sfcl_assembly, sfcl_chromosome,
        sfcl_chromosome_reference_accession_number, sfcl_evidence_code)
    SELECT get_id_and_insert_active_data('SFCL'), v.var_feature_zdb_id, v.var_zdb_id,
           pos.start_position, pos.end_position, 'GRCz12tu', '16',
           'NC_133191.1', 'ZDB-TERM-170419-250'
      FROM new_variant v
      JOIN (VALUES (1, 46433934, 46433942),
                   (2, 46433969, 46433969)) AS pos (variant_number, start_position, end_position)
        ON pos.variant_number = v.var_variant_number
    RETURNING sfcl_zdb_id
)
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
SELECT sfcl_zdb_id, 'ZDB-PUB-220901-7' FROM new_location
UNION ALL
SELECT fgmd_zdb_id, 'ZDB-PUB-220901-7' FROM new_genomic_detail;
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT s.sfcl_zdb_id FROM sequence_feature_chromosome_location s JOIN variant v ON v.var_zdb_id = s.sfcl_variant_zdb_id WHERE v.var_feature_zdb_id = 'ZDB-ALT-230227-4');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT g.fgmd_zdb_id FROM feature_genomic_mutation_detail g JOIN variant v ON v.var_zdb_id = g.fgmd_variant_zdb_id WHERE v.var_feature_zdb_id = 'ZDB-ALT-230227-4');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT var_zdb_id FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-230227-4');
