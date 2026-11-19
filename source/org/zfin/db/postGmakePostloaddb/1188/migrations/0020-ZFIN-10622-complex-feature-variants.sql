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


-- ─── bns497 and bns426: DNA, transcript and protein details per variant ──
-- From ZDB-PUB-220901-7: the deletions are described in the Methods (tal1 bns497 "-7 bp
-- (out-of-frame) ... premature termination codon after R228 ... truncate the protein sequence
-- by 97 amino acids", fourth exon; etsrp bns426 "-9 bp (in-frame) ... loss of D265 G266 and
-- W267", sixth exon); the point mutations are in Fig. S11. Positions are on the RefSeq
-- transcripts and proteins, checked against their sequences: tal1 NM_213237.1 / NP_998402.1
-- (324 aa), etsrp NM_001037375.1 / NP_001032452.1 (366 aa). Exon numbers count exon 1, which
-- is 5' UTR in both, so the paper's "sixth exon" of etsrp (coding exons) is exon 7 here.
--
--   bns497 V1  c.683_689del (GTCTGGC)  exon 4  frameshift_variant, stop_gained   p.Arg228ProfsTer2: Arg228>Pro, 97 aa lost
--   bns497 V2  c.706T>C                exon 4  missense_variant                  p.Phe236Leu (after V1's stop at codon 229)
--   bns426 V1  c.795_803del (TGGCTGGGA) exon 7 inframe_deletion                  p.Asp265_Trp267del, 3 aa removed
--   bns426 V2  c.768C>T                exon 7  (synonymous: no ZFIN transcript consequence term; protein unchanged, p.Cys256=)
--changeset cmpich:ZFIN-10622-bns497-bns426-mutation-details
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:4 SELECT count(*) FROM variant WHERE var_feature_zdb_id IN ('ZDB-ALT-230227-6', 'ZDB-ALT-230227-4')
--precondition-sql-check expectedResult:0 SELECT count(*) FROM feature_dna_mutation_detail JOIN variant ON var_zdb_id = fdmd_variant_zdb_id WHERE var_feature_zdb_id IN ('ZDB-ALT-230227-6', 'ZDB-ALT-230227-4')
--precondition-sql-check expectedResult:0 SELECT count(*) FROM feature_transcript_mutation_detail JOIN variant ON var_zdb_id = ftmd_variant_zdb_id WHERE var_feature_zdb_id IN ('ZDB-ALT-230227-6', 'ZDB-ALT-230227-4')
--precondition-sql-check expectedResult:0 SELECT count(*) FROM feature_protein_mutation_detail JOIN variant ON var_zdb_id = fpmd_variant_zdb_id WHERE var_feature_zdb_id IN ('ZDB-ALT-230227-6', 'ZDB-ALT-230227-4')
WITH vocab AS (
    SELECT t.term_ont_id, t.term_zdb_id
      FROM mutation_detail_controlled_vocabulary
      JOIN term t ON t.term_zdb_id = mdcv_term_zdb_id
), new_dna AS (
    INSERT INTO feature_dna_mutation_detail (
        fdmd_zdb_id, fdmd_variant_zdb_id, fdmd_dna_mutation_term_zdb_id,
        fdmd_dna_sequence_of_reference_accession_number, fdmd_fdbcont_zdb_id,
        fdmd_dna_position_start, fdmd_dna_position_end, fdmd_number_removed_dna_base_pairs,
        fdmd_deleted_sequence, fdmd_exon_number, fdmd_gene_localization_term_zdb_id)
    SELECT get_id_and_insert_active_data('FDMD'), v.var_zdb_id,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = d.dna_term),
           d.accession, 'ZDB-FDBCONT-040412-38',      -- RefSeq RNA
           d.position_start, d.position_end, d.removed, d.deleted_sequence, d.exon,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = 'SO:0000147')   -- exon
      FROM (VALUES ('ZDB-ALT-230227-6', 1, NULL,         'NM_213237.1',    683, 689, 7,    'GTCTGGC',   4),
                   ('ZDB-ALT-230227-6', 2, 'SO:1000013', 'NM_213237.1',    706, 706, NULL, NULL,        4),   -- T_to_C_transition
                   ('ZDB-ALT-230227-4', 1, NULL,         'NM_001037375.1', 795, 803, 9,    'TGGCTGGGA', 7),
                   ('ZDB-ALT-230227-4', 2, 'SO:1000011', 'NM_001037375.1', 768, 768, NULL, NULL,        7))   -- C_to_T_transition
           AS d (feature_zdb_id, variant_number, dna_term, accession, position_start, position_end, removed, deleted_sequence, exon)
      JOIN variant v ON v.var_feature_zdb_id = d.feature_zdb_id AND v.var_variant_number = d.variant_number
    RETURNING fdmd_zdb_id
), new_transcript AS (
    INSERT INTO feature_transcript_mutation_detail (
        ftmd_zdb_id, ftmd_variant_zdb_id, ftmd_transcript_consequence_term_zdb_id, ftmd_exon_number)
    SELECT get_id_and_insert_active_data('FTMD'), v.var_zdb_id,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = t.consequence), t.exon
      FROM (VALUES ('ZDB-ALT-230227-6', 1, 'SO:0001589', 4),   -- frameshift_variant
                   ('ZDB-ALT-230227-6', 1, 'SO:0001587', 4),   -- stop_gained
                   ('ZDB-ALT-230227-6', 2, 'SO:0001583', 4),   -- missense_variant
                   ('ZDB-ALT-230227-4', 1, 'SO:0001822', 7))   -- inframe_deletion
           AS t (feature_zdb_id, variant_number, consequence, exon)
      JOIN variant v ON v.var_feature_zdb_id = t.feature_zdb_id AND v.var_variant_number = t.variant_number
    RETURNING ftmd_zdb_id
), new_protein AS (
    INSERT INTO feature_protein_mutation_detail (
        fpmd_zdb_id, fpmd_variant_zdb_id, fpmd_protein_consequence_term_zdb_id,
        fpmd_sequence_of_reference_accession_number, fpmd_fdbcont_zdb_id,
        fpmd_protein_position_start, fpmd_protein_position_end,
        fpmd_wt_protein_term_zdb_id, fpmd_mutant_or_stop_protein_term_zdb_id,
        fpmd_number_amino_acids_removed)
    SELECT get_id_and_insert_active_data('FPMD'), v.var_zdb_id,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = p.consequence),
           p.accession, 'ZDB-FDBCONT-040412-39',      -- RefSeq Polypeptide
           p.position_start, p.position_end,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = p.wt_aa),
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = p.mutant_aa),
           p.removed
      FROM (VALUES ('ZDB-ALT-230227-6', 1, 'SO:0001617', 'NP_998402.1',    228, 228, 'SO:0001451', 'SO:0001439', 97),    -- polypeptide_truncation, arginine > proline
                   ('ZDB-ALT-230227-6', 2, 'SO:0001606', 'NP_998402.1',    236, 236, 'SO:0001441', 'SO:0001437', NULL),  -- amino_acid_substitution, phenylalanine > leucine
                   ('ZDB-ALT-230227-4', 1, 'SO:0001604', 'NP_001032452.1', 265, 267, NULL,         NULL,         3))     -- amino_acid_deletion
           AS p (feature_zdb_id, variant_number, consequence, accession, position_start, position_end, wt_aa, mutant_aa, removed)
      JOIN variant v ON v.var_feature_zdb_id = p.feature_zdb_id AND v.var_variant_number = p.variant_number
    RETURNING fpmd_zdb_id
)
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
SELECT fdmd_zdb_id, 'ZDB-PUB-220901-7' FROM new_dna
UNION ALL
SELECT ftmd_zdb_id, 'ZDB-PUB-220901-7' FROM new_transcript
UNION ALL
SELECT fpmd_zdb_id, 'ZDB-PUB-220901-7' FROM new_protein;
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT fdmd_zdb_id FROM feature_dna_mutation_detail JOIN variant ON var_zdb_id = fdmd_variant_zdb_id WHERE var_feature_zdb_id IN ('ZDB-ALT-230227-6', 'ZDB-ALT-230227-4'));
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT ftmd_zdb_id FROM feature_transcript_mutation_detail JOIN variant ON var_zdb_id = ftmd_variant_zdb_id WHERE var_feature_zdb_id IN ('ZDB-ALT-230227-6', 'ZDB-ALT-230227-4'));
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT fpmd_zdb_id FROM feature_protein_mutation_detail JOIN variant ON var_zdb_id = fpmd_variant_zdb_id WHERE var_feature_zdb_id IN ('ZDB-ALT-230227-6', 'ZDB-ALT-230227-4'));


-- ─── gl12: three point mutations with their GRCz12tu locations and base changes ─
-- gl12 (ZDB-ALT-170531-3, Complex, zbtb11), chr6. Source ZDB-PUB-170407-2 (Keightley et al. 2017,
-- Nat Commun, doi:10.1038/ncomms14911). The feature's variant note gives the three changes on
-- GRCz11 chr6 (29159643 T>G, 29159654 C>A, 29159663 C>G); the reference bases were checked there,
-- and the 71 bp around them (GRCz11 29159620-29159690, covering the CRISPR1-zbtb11 target) matches
-- GRCz12tu chr6 once, at 31663658, a shift of +2504038. Bases are on the GRCz12tu + strand.
--   Variant 1: Point Mutation  31663681  T > G
--   Variant 2: Point Mutation  31663692  C > A
--   Variant 3: Point Mutation  31663701  C > G
--changeset cmpich:ZFIN-10622-gl12-variants
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:1 SELECT count(*) FROM feature WHERE feature_zdb_id = 'ZDB-ALT-170531-3' AND feature_abbrev = 'gl12'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-170531-3'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM sequence_feature_chromosome_location WHERE sfcl_feature_zdb_id = 'ZDB-ALT-170531-3'
-- Same conventions as bns497: evidence ECO:0000304 (TAS), attributed to the feature's publication.
WITH new_variant AS (
    INSERT INTO variant (var_zdb_id, var_feature_zdb_id, var_variant_number, var_variant_type)
    VALUES (get_id_and_insert_active_data('VAR'), 'ZDB-ALT-170531-3', 1, 'POINT_MUTATION'),
           (get_id_and_insert_active_data('VAR'), 'ZDB-ALT-170531-3', 2, 'POINT_MUTATION'),
           (get_id_and_insert_active_data('VAR'), 'ZDB-ALT-170531-3', 3, 'POINT_MUTATION')
    RETURNING var_zdb_id, var_feature_zdb_id, var_variant_number
), new_genomic_detail AS (
    INSERT INTO feature_genomic_mutation_detail (
        fgmd_zdb_id, fgmd_variant_zdb_id,
        fgmd_sequence_of_reference, fgmd_sequence_of_variation, fgmd_variation_strand)
    SELECT get_id_and_insert_active_data('FGMD'), v.var_zdb_id,
           seq.reference, seq.variation, '+'
      FROM new_variant v
      JOIN (VALUES (1, 'T', 'G'),
                   (2, 'C', 'A'),
                   (3, 'C', 'G')) AS seq (variant_number, reference, variation)
        ON seq.variant_number = v.var_variant_number
    RETURNING fgmd_zdb_id
), new_location AS (
    INSERT INTO sequence_feature_chromosome_location (
        sfcl_zdb_id, sfcl_feature_zdb_id, sfcl_variant_zdb_id,
        sfcl_start_position, sfcl_end_position, sfcl_assembly, sfcl_chromosome,
        sfcl_chromosome_reference_accession_number, sfcl_evidence_code)
    SELECT get_id_and_insert_active_data('SFCL'), v.var_feature_zdb_id, v.var_zdb_id,
           pos.position, pos.position, 'GRCz12tu', '6',
           'NC_133181.1', 'ZDB-TERM-170419-250'
      FROM new_variant v
      JOIN (VALUES (1, 31663681),
                   (2, 31663692),
                   (3, 31663701)) AS pos (variant_number, position)
        ON pos.variant_number = v.var_variant_number
    RETURNING sfcl_zdb_id
)
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
SELECT sfcl_zdb_id, 'ZDB-PUB-170407-2' FROM new_location
UNION ALL
SELECT fgmd_zdb_id, 'ZDB-PUB-170407-2' FROM new_genomic_detail;
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT s.sfcl_zdb_id FROM sequence_feature_chromosome_location s JOIN variant v ON v.var_zdb_id = s.sfcl_variant_zdb_id WHERE v.var_feature_zdb_id = 'ZDB-ALT-170531-3');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT g.fgmd_zdb_id FROM feature_genomic_mutation_detail g JOIN variant v ON v.var_zdb_id = g.fgmd_variant_zdb_id WHERE v.var_feature_zdb_id = 'ZDB-ALT-170531-3');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT var_zdb_id FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-170531-3');


-- ─── gl12: DNA, transcript and protein details per variant ──────────────
-- zbtb11 is on the - strand; all three changes are in the coding part of exon 1 (of 10) of
-- NM_212650.2 / NP_997815.2 (1146 aa), checked against the GRCz12tu RefSeq annotation. DNA terms
-- are on the transcript strand, as for bns426.
--   V1  c.77A>C  exon 1  missense_variant  p.His26Pro (CAC > CCC)
--   V2  c.66G>T  exon 1  (synonymous: no ZFIN transcript consequence term; protein unchanged, p.Ala22=)
--   V3  c.57G>C  exon 1  missense_variant  p.Glu19Asp (GAG > GAC)
-- The authors' "c.80-87delinsACG" (curator note on the feature) does not fit these changes and is
-- not used.
--changeset cmpich:ZFIN-10622-gl12-mutation-details
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:3 SELECT count(*) FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-170531-3'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM feature_dna_mutation_detail JOIN variant ON var_zdb_id = fdmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-170531-3'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM feature_transcript_mutation_detail JOIN variant ON var_zdb_id = ftmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-170531-3'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM feature_protein_mutation_detail JOIN variant ON var_zdb_id = fpmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-170531-3'
WITH vocab AS (
    SELECT t.term_ont_id, t.term_zdb_id
      FROM mutation_detail_controlled_vocabulary
      JOIN term t ON t.term_zdb_id = mdcv_term_zdb_id
), new_dna AS (
    INSERT INTO feature_dna_mutation_detail (
        fdmd_zdb_id, fdmd_variant_zdb_id, fdmd_dna_mutation_term_zdb_id,
        fdmd_dna_sequence_of_reference_accession_number, fdmd_fdbcont_zdb_id,
        fdmd_dna_position_start, fdmd_dna_position_end, fdmd_exon_number,
        fdmd_gene_localization_term_zdb_id)
    SELECT get_id_and_insert_active_data('FDMD'), v.var_zdb_id,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = d.dna_term),
           'NM_212650.2', 'ZDB-FDBCONT-040412-38',    -- RefSeq RNA
           d.position, d.position, 1,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = 'SO:0000147')   -- exon
      FROM (VALUES (1, 'SO:1000024', 77),   -- A_to_C_transversion
                   (2, 'SO:1000027', 66),   -- G_to_T_transversion
                   (3, 'SO:1000026', 57))   -- G_to_C_transversion
           AS d (variant_number, dna_term, position)
      JOIN variant v ON v.var_feature_zdb_id = 'ZDB-ALT-170531-3' AND v.var_variant_number = d.variant_number
    RETURNING fdmd_zdb_id
), new_transcript AS (
    INSERT INTO feature_transcript_mutation_detail (
        ftmd_zdb_id, ftmd_variant_zdb_id, ftmd_transcript_consequence_term_zdb_id, ftmd_exon_number)
    SELECT get_id_and_insert_active_data('FTMD'), v.var_zdb_id,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = 'SO:0001583'), 1   -- missense_variant
      FROM variant v
     WHERE v.var_feature_zdb_id = 'ZDB-ALT-170531-3' AND v.var_variant_number IN (1, 3)
    RETURNING ftmd_zdb_id
), new_protein AS (
    INSERT INTO feature_protein_mutation_detail (
        fpmd_zdb_id, fpmd_variant_zdb_id, fpmd_protein_consequence_term_zdb_id,
        fpmd_sequence_of_reference_accession_number, fpmd_fdbcont_zdb_id,
        fpmd_protein_position_start, fpmd_protein_position_end,
        fpmd_wt_protein_term_zdb_id, fpmd_mutant_or_stop_protein_term_zdb_id)
    SELECT get_id_and_insert_active_data('FPMD'), v.var_zdb_id,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = 'SO:0001606'),   -- amino_acid_substitution
           'NP_997815.2', 'ZDB-FDBCONT-040412-39',    -- RefSeq Polypeptide
           p.position, p.position,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = p.wt_aa),
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = p.mutant_aa)
      FROM (VALUES (1, 26, 'SO:0001452', 'SO:0001439'),    -- histidine > proline
                   (3, 19, 'SO:0001454', 'SO:0001453'))    -- glutamic_acid > aspartic_acid
           AS p (variant_number, position, wt_aa, mutant_aa)
      JOIN variant v ON v.var_feature_zdb_id = 'ZDB-ALT-170531-3' AND v.var_variant_number = p.variant_number
    RETURNING fpmd_zdb_id
)
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
SELECT fdmd_zdb_id, 'ZDB-PUB-170407-2' FROM new_dna
UNION ALL
SELECT ftmd_zdb_id, 'ZDB-PUB-170407-2' FROM new_transcript
UNION ALL
SELECT fpmd_zdb_id, 'ZDB-PUB-170407-2' FROM new_protein;
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT fdmd_zdb_id FROM feature_dna_mutation_detail JOIN variant ON var_zdb_id = fdmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-170531-3');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT ftmd_zdb_id FROM feature_transcript_mutation_detail JOIN variant ON var_zdb_id = ftmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-170531-3');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT fpmd_zdb_id FROM feature_protein_mutation_detail JOIN variant ON var_zdb_id = fpmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-170531-3');


-- ─── tsu7cd: a deletion and a point mutation with their GRCz12tu locations ─
-- tsu7cd (ZDB-ALT-180327-2, Complex, cdc6), chr11. Source ZDB-PUB-171007-1 (Yao et al. 2017,
-- Hum Mol Genet, doi:10.1093/hmg/ddx305). The feature's variant note gives the changes on GRCz11
-- chr11 (deletion 11791316-11791322, 7 bp; point mutation 11791342 G>A); the reference bases were
-- checked there (GCTTAGG, G), and the 81 bp around them (GRCz11 11791290-11791370, covering the
-- CRISPR1-cdc6 target) matches GRCz12tu chr11 once, at 12162153, a shift of +370863. Bases are on
-- the GRCz12tu + strand; variants are numbered by position, as for bns426.
--   Variant 1: Small Deletion  12162179-12162185  GCTTAGG > -  (equivalent: 12162180-12162186)
--   Variant 2: Point Mutation  12162205           G > A
-- The feature already has variant 1 (made by ZFIN-10622-migrate-existing-variants, no type); it
-- gets its type here, and variant 2 is new.
--changeset cmpich:ZFIN-10622-tsu7cd-variants
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:1 SELECT count(*) FROM feature WHERE feature_zdb_id = 'ZDB-ALT-180327-2' AND feature_abbrev = 'tsu7cd'
--precondition-sql-check expectedResult:1 SELECT count(*) FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2' AND var_variant_number = 1 AND var_variant_type IS NULL
--precondition-sql-check expectedResult:1 SELECT count(*) FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM sequence_feature_chromosome_location WHERE sfcl_feature_zdb_id = 'ZDB-ALT-180327-2'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM feature_genomic_mutation_detail JOIN variant ON var_zdb_id = fgmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2'
-- Same conventions as bns497: evidence ECO:0000304 (TAS), attributed to the feature's publication,
-- deletion's sequence of variation NULL.
WITH typed_variant AS (
    UPDATE variant SET var_variant_type = 'DELETION'
     WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2' AND var_variant_number = 1
    RETURNING var_zdb_id, var_feature_zdb_id, var_variant_number
), new_variant AS (
    INSERT INTO variant (var_zdb_id, var_feature_zdb_id, var_variant_number, var_variant_type)
    VALUES (get_id_and_insert_active_data('VAR'), 'ZDB-ALT-180327-2', 2, 'POINT_MUTATION')
    RETURNING var_zdb_id, var_feature_zdb_id, var_variant_number
), all_variant AS (
    SELECT * FROM typed_variant
    UNION ALL
    SELECT * FROM new_variant
), new_genomic_detail AS (
    INSERT INTO feature_genomic_mutation_detail (
        fgmd_zdb_id, fgmd_variant_zdb_id,
        fgmd_sequence_of_reference, fgmd_sequence_of_variation, fgmd_variation_strand)
    SELECT get_id_and_insert_active_data('FGMD'), v.var_zdb_id,
           seq.reference, seq.variation, '+'
      FROM all_variant v
      JOIN (VALUES (1, 'GCTTAGG', NULL),
                   (2, 'G',       'A')) AS seq (variant_number, reference, variation)
        ON seq.variant_number = v.var_variant_number
    RETURNING fgmd_zdb_id
), new_location AS (
    INSERT INTO sequence_feature_chromosome_location (
        sfcl_zdb_id, sfcl_feature_zdb_id, sfcl_variant_zdb_id,
        sfcl_start_position, sfcl_end_position, sfcl_assembly, sfcl_chromosome,
        sfcl_chromosome_reference_accession_number, sfcl_evidence_code)
    SELECT get_id_and_insert_active_data('SFCL'), v.var_feature_zdb_id, v.var_zdb_id,
           pos.start_position, pos.end_position, 'GRCz12tu', '11',
           'NC_133186.1', 'ZDB-TERM-170419-250'
      FROM all_variant v
      JOIN (VALUES (1, 12162179, 12162185),
                   (2, 12162205, 12162205)) AS pos (variant_number, start_position, end_position)
        ON pos.variant_number = v.var_variant_number
    RETURNING sfcl_zdb_id
)
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
SELECT sfcl_zdb_id, 'ZDB-PUB-171007-1' FROM new_location
UNION ALL
SELECT fgmd_zdb_id, 'ZDB-PUB-171007-1' FROM new_genomic_detail;
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT s.sfcl_zdb_id FROM sequence_feature_chromosome_location s JOIN variant v ON v.var_zdb_id = s.sfcl_variant_zdb_id WHERE v.var_feature_zdb_id = 'ZDB-ALT-180327-2');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT g.fgmd_zdb_id FROM feature_genomic_mutation_detail g JOIN variant v ON v.var_zdb_id = g.fgmd_variant_zdb_id WHERE v.var_feature_zdb_id = 'ZDB-ALT-180327-2');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT var_zdb_id FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2' AND var_variant_number = 2);
--rollback UPDATE variant SET var_variant_type = NULL WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2' AND var_variant_number = 1;


-- ─── tsu7cd: DNA, transcript and protein details per variant ────────────
-- cdc6 is on the - strand; both changes are in exon 3 (of 12) of NM_001127333.1 / NP_001120805.1
-- (561 aa), checked against the GRCz12tu RefSeq annotation. DNA terms are on the transcript strand.
-- The deletion is placed at its 3'-most position on the transcript.
--   V1  c.286_292del (CCTAAGC)  exon 3  frameshift_variant, stop_gained   p.Pro96SerfsTer47: Pro96>Ser, 141 aa left, 420 aa lost
--   V2  c.266C>T                exon 3  missense_variant                  p.Ala89Val
-- With both, the mutant protein is 141 aa: Val89, then the frameshift from codon 96.
-- The feature's existing stop_gained transcript consequence (ZDB-FTMD-180709-1, now on variant 1)
-- is kept and given its exon number.
--changeset cmpich:ZFIN-10622-tsu7cd-mutation-details
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:2 SELECT count(*) FROM variant WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM feature_dna_mutation_detail JOIN variant ON var_zdb_id = fdmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2'
--precondition-sql-check expectedResult:1 SELECT count(*) FROM feature_transcript_mutation_detail JOIN variant ON var_zdb_id = ftmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2' AND var_variant_number = 1 AND ftmd_zdb_id = 'ZDB-FTMD-180709-1' AND ftmd_exon_number IS NULL
--precondition-sql-check expectedResult:1 SELECT count(*) FROM feature_transcript_mutation_detail JOIN variant ON var_zdb_id = ftmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2'
--precondition-sql-check expectedResult:0 SELECT count(*) FROM feature_protein_mutation_detail JOIN variant ON var_zdb_id = fpmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2'
WITH vocab AS (
    SELECT t.term_ont_id, t.term_zdb_id
      FROM mutation_detail_controlled_vocabulary
      JOIN term t ON t.term_zdb_id = mdcv_term_zdb_id
), existing_transcript AS (
    -- runs even though nothing reads it; the row is already attributed to ZDB-PUB-171007-1
    UPDATE feature_transcript_mutation_detail SET ftmd_exon_number = 3
     WHERE ftmd_zdb_id = 'ZDB-FTMD-180709-1'
    RETURNING ftmd_zdb_id
), new_dna AS (
    INSERT INTO feature_dna_mutation_detail (
        fdmd_zdb_id, fdmd_variant_zdb_id, fdmd_dna_mutation_term_zdb_id,
        fdmd_dna_sequence_of_reference_accession_number, fdmd_fdbcont_zdb_id,
        fdmd_dna_position_start, fdmd_dna_position_end, fdmd_number_removed_dna_base_pairs,
        fdmd_deleted_sequence, fdmd_exon_number, fdmd_gene_localization_term_zdb_id)
    SELECT get_id_and_insert_active_data('FDMD'), v.var_zdb_id,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = d.dna_term),
           'NM_001127333.1', 'ZDB-FDBCONT-040412-38',      -- RefSeq RNA
           d.position_start, d.position_end, d.removed, d.deleted_sequence, 3,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = 'SO:0000147')   -- exon
      FROM (VALUES (1, NULL,         286, 292, 7,    'CCTAAGC'),
                   (2, 'SO:1000011', 266, 266, NULL, NULL))      -- C_to_T_transition
           AS d (variant_number, dna_term, position_start, position_end, removed, deleted_sequence)
      JOIN variant v ON v.var_feature_zdb_id = 'ZDB-ALT-180327-2' AND v.var_variant_number = d.variant_number
    RETURNING fdmd_zdb_id
), new_transcript AS (
    INSERT INTO feature_transcript_mutation_detail (
        ftmd_zdb_id, ftmd_variant_zdb_id, ftmd_transcript_consequence_term_zdb_id, ftmd_exon_number)
    SELECT get_id_and_insert_active_data('FTMD'), v.var_zdb_id,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = t.consequence), 3
      FROM (VALUES (1, 'SO:0001589'),    -- frameshift_variant (stop_gained is the existing ZDB-FTMD-180709-1)
                   (2, 'SO:0001583'))    -- missense_variant
           AS t (variant_number, consequence)
      JOIN variant v ON v.var_feature_zdb_id = 'ZDB-ALT-180327-2' AND v.var_variant_number = t.variant_number
    RETURNING ftmd_zdb_id
), new_protein AS (
    INSERT INTO feature_protein_mutation_detail (
        fpmd_zdb_id, fpmd_variant_zdb_id, fpmd_protein_consequence_term_zdb_id,
        fpmd_sequence_of_reference_accession_number, fpmd_fdbcont_zdb_id,
        fpmd_protein_position_start, fpmd_protein_position_end,
        fpmd_wt_protein_term_zdb_id, fpmd_mutant_or_stop_protein_term_zdb_id,
        fpmd_number_amino_acids_removed)
    SELECT get_id_and_insert_active_data('FPMD'), v.var_zdb_id,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = p.consequence),
           'NP_001120805.1', 'ZDB-FDBCONT-040412-39',      -- RefSeq Polypeptide
           p.position, p.position,
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = p.wt_aa),
           (SELECT term_zdb_id FROM vocab WHERE term_ont_id = p.mutant_aa),
           p.removed
      FROM (VALUES (1, 'SO:0001617', 96, 'SO:0001439', 'SO:0001444', 420),    -- polypeptide_truncation, proline > serine
                   (2, 'SO:0001606', 89, 'SO:0001435', 'SO:0001436', NULL))   -- amino_acid_substitution, alanine > valine
           AS p (variant_number, consequence, position, wt_aa, mutant_aa, removed)
      JOIN variant v ON v.var_feature_zdb_id = 'ZDB-ALT-180327-2' AND v.var_variant_number = p.variant_number
    RETURNING fpmd_zdb_id
)
INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
SELECT fdmd_zdb_id, 'ZDB-PUB-171007-1' FROM new_dna
UNION ALL
SELECT ftmd_zdb_id, 'ZDB-PUB-171007-1' FROM new_transcript
UNION ALL
SELECT fpmd_zdb_id, 'ZDB-PUB-171007-1' FROM new_protein;
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT fdmd_zdb_id FROM feature_dna_mutation_detail JOIN variant ON var_zdb_id = fdmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT ftmd_zdb_id FROM feature_transcript_mutation_detail JOIN variant ON var_zdb_id = ftmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2' AND ftmd_zdb_id <> 'ZDB-FTMD-180709-1');
--rollback DELETE FROM zdb_active_data WHERE zactvd_zdb_id IN (SELECT fpmd_zdb_id FROM feature_protein_mutation_detail JOIN variant ON var_zdb_id = fpmd_variant_zdb_id WHERE var_feature_zdb_id = 'ZDB-ALT-180327-2');
--rollback UPDATE feature_transcript_mutation_detail SET ftmd_exon_number = NULL WHERE ftmd_zdb_id = 'ZDB-FTMD-180709-1';
