--liquibase formatted sql

-- B8a: replace the placeholder lesion columns from the initial flat schema
-- with a set named after the xlsx field matrix (more-product-specs/Field
-- summary of Molecular nature of the mutation.xlsx). The form picks a
-- lesion type up front and shows only the fields that apply; the columns
-- here line up 1:1 with those field names.
--
-- The new columns:
--   l_lesion_size_bp     — single size (bp) for Point / Deletion / Insertion / Indel / Transgene
--   l_nucleotide_change  — Point mutation: WT → mutant nucleotide(s); also Indel
--   l_deleted_sequence   — Deletion: sequence of the deleted region
--   l_inserted_sequence  — Insertion: sequence of the inserted fragment (also Indel's inserted side)
--   l_transgene_sequence — Transgene: sequence of the transgene
--   l_location_inline    — Point + small variants: location annotated inline (~5 nt flanks)
--   l_5prime_flank       — Large variants + Transgene: structured 5' flank (~20 nt)
--   l_3prime_flank       — Large variants + Transgene: structured 3' flank (~20 nt)
--   l_has_large_variant  — Toggle from the curator: when true, show the structured flank rows
--
-- The dropped columns were B3c placeholders for the flat-schema iteration;
-- nothing in production has populated them, so a destructive change is fine.

--changeset rtaylor:zirc-b8a-lesion-typed-columns
ALTER TABLE zirc.lesion
    DROP COLUMN l_index_deletion_pos,
    DROP COLUMN l_index_insertion_size,
    DROP COLUMN l_deleted_base_pairs,
    DROP COLUMN l_inserted_base_pairs,
    DROP COLUMN l_wt_genomic_sequence,
    ADD COLUMN l_lesion_size_bp     INTEGER,
    ADD COLUMN l_nucleotide_change  TEXT,
    ADD COLUMN l_deleted_sequence   TEXT,
    ADD COLUMN l_inserted_sequence  TEXT,
    ADD COLUMN l_transgene_sequence TEXT,
    ADD COLUMN l_location_inline    TEXT,
    ADD COLUMN l_5prime_flank       TEXT,
    ADD COLUMN l_3prime_flank       TEXT,
    ADD COLUMN l_has_large_variant  BOOLEAN;
