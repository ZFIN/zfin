--liquibase formatted sql

-- ZFIN-10400: capture where an insertion came from.
--
-- Two yes/no questions on the lesion, each revealing its own follow-up:
--   mutagenesis (CRISPR or TALEN)  -> CRISPR sequence, TALEN sequence
--   construct or other species DNA -> construct name
--
-- Both flags are nullable three-state: null means unanswered, which is
-- distinct from "no". The ticket requires at least one of the two to be
-- answered, and that requirement is surfaced as a status badge by
-- LesionStatusComputer -- nullable is what lets "not yet answered" be
-- detectable at all.
--
-- No check constraint enforcing "at least one answered". The form autosaves
-- field by field, so a row is legitimately half-filled for as long as a
-- curator is working on it; a constraint would reject the first PATCH of a
-- pair. Submission-time validation is a separate piece of work the ticket
-- explicitly defers.
--
-- The two sequence columns are plain text like the existing lesion sequence
-- columns, not a constrained domain: normalization to bases happens in
-- ZircLesionFormSchema on write, so the same code path guards the form and a
-- direct PATCH.

--changeset zirc:zfin-10400-lesion-insertion-origin

alter table zirc.lesion
    add column if not exists l_insertion_from_mutagenesis boolean,
    add column if not exists l_insertion_from_construct   boolean,
    add column if not exists l_crispr_sequence            text,
    add column if not exists l_talen_sequence             text,
    add column if not exists l_construct_name             text;
