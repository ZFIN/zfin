--liquibase formatted sql

-- ZFIN-10399: record transcript consequences per ZIRC lesion.
--
-- Multi-valued, and available on every lesion type rather than gated by the
-- type matrix the way the sequence clusters are.
--
-- A text[] column rather than a child table, following ls_previous_names
-- (0090-zirc-previous-names-array.sql): the form's autosave diff treats an
-- array as one atomic leaf and PATCHes the whole list, so a child table would
-- add a join and a row-identity problem to something the UI already handles
-- as a single value.
--
-- Values are mutation_detail_controlled_vocabulary term ZDB IDs, not display
-- names, so renaming a term cannot orphan stored data and the eventual
-- ZIRC-to-curation load already has the identifier it needs. No foreign key:
-- Postgres cannot constrain array elements, and the alternative (a child
-- table purely to hold the FK) is the design rejected above. The form only
-- offers ids from /api/zirc/vocabulary, and an id that stops resolving still
-- renders -- VocabularyMultiSelectRenderer falls back to showing the raw id
-- rather than dropping it silently.

--changeset zirc:zfin-10399-lesion-transcript-consequences

alter table zirc.lesion
    add column if not exists l_transcript_consequences text[] not null default '{}';
