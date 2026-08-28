--liquibase formatted sql

-- ZFIN-10400 revision: one "check all that apply" origin list, replacing the
-- pair of yes/no questions.
--
-- The two questions expressed one choice awkwardly, and answering yes to the
-- mutagenesis one opened both a CRISPR and a TALEN box when only one applies.
-- A checklist names the mechanism directly and reveals just the input that
-- belongs to it.
--
-- Multi-valued rather than a single choice because origins combine: a
-- CRISPR-mediated knock-in of a reporter construct is both CRISPR and
-- construct, and that is ordinary practice. The two booleans could express it
-- (yes to both); a single-select could not, so this keeps what was there.
--
-- Values are stable tokens -- crispr, talen, construct, other, unknown --
-- rather than mdcv term ids. This is not a curation vocabulary; it has no
-- Sequence Ontology counterpart and is specific to the ZIRC submission form.
--
-- l_insertion_origin_other carries the free text for "other", which covers
-- transposon, retroviral and spontaneous insertions that the named options
-- miss. "unknown" is deliberately distinct from leaving the field blank:
-- blank means unanswered, unknown means answered as not known.
--
-- The two boolean columns are dropped. They were added days ago in this same
-- unreleased batch (0030-ZFIN-10400) and hold no data outside development, so
-- there is nothing to preserve; leaving them would be two dead columns whose
-- meaning is superseded. A separate changeset from 0030 because that one has
-- already run -- editing it would change its checksum.

--changeset zirc:zfin-10400-insertion-origin-checklist

alter table zirc.lesion
    add column if not exists l_insertion_origins     text[] not null default '{}',
    add column if not exists l_insertion_origin_other text;

alter table zirc.lesion
    drop column if exists l_insertion_from_mutagenesis,
    drop column if exists l_insertion_from_construct;
