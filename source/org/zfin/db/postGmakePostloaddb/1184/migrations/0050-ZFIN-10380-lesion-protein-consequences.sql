--liquibase formatted sql

-- ZFIN-10380: record protein consequences per ZIRC lesion.
--
-- Multi-valued, on the same lesion types as the amino-acid change it sits
-- with. text[] of protein_consequence_term ZDB IDs, following
-- l_transcript_consequences (0020-ZFIN-10399) -- see that file for why an
-- array rather than a child table, and ids rather than display names.
--
-- No vocabulary reordering here, unlike its ZFIN-10399 counterpart. The
-- ticket's list differs from what is stored only by the two new terms:
--
--   stored today          ticket's list
--   1 polypeptide truncation        1 polypeptide truncation
--                                   . c-terminal peptide truncation   (new)
--                                   . n-terminal peptide truncation   (new)
--   2 amino acid substitution       2 amino acid substitution
--   3 amino acid deletion           3 amino acid deletion
--   4 amino acid insertion          4 amino acid insertion
--   5 non conservative amino ...    5 non conservative amino ...
--   6 elongated polypeptide         6 elongated polypeptide
--   7 polypeptide fusion            7 polypeptide fusion
--
-- The seven that exist are already in the requested relative order, with no
-- duplicate mdcv_term_order values among them, so there is nothing to fix.
-- Renumbering purely to leave gaps at 2 and 3 would churn shared curation
-- data for no visible effect; the follow-up that adds the terms can
-- renumber then.
--
-- DEFERRED (ZFIN-10380 follow-up): "c-terminal peptide truncation" and
-- "n-terminal peptide truncation" have no Sequence Ontology term in the
-- `term` table. The nearest loaded candidates -- SO:0001617
-- polypeptide_truncation (already used by the first row), SO:0001906
-- feature_truncation, SO:1000098 sequence_variant_causing_polypeptide_
-- truncation -- none of them mean N- or C-terminal. mdcv_term_zdb_id is a
-- foreign key to `term`, which the OBO loads own, so a hand-inserted row
-- would be wiped by the next ontology load. Curators need to choose the
-- backing terms before these two can be added.

--changeset zirc:zfin-10380-lesion-protein-consequences

alter table zirc.lesion
    add column if not exists l_protein_consequences text[] not null default '{}';
