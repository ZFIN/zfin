--liquibase formatted sql

-- ZFIN-10379: structured amino-acid change, replacing the free-text box.
--
-- from > to residues plus a position, mirroring the curation interface's
-- control instead of asking curators to hand-write HGVS into a text field.
--
-- The residues are amino_acid_term ZDB IDs, not display names or one-letter
-- codes, matching how l_transcript_consequences stores its vocabulary. "Stop"
-- is a member of that vocabulary, so a nonsense mutation is an ordinary value
-- here rather than a special case.
--
-- Position is a start/end pair. The ticket text says "position input box"
-- singular, but its screenshot shows a start-end pair and asks for something
-- similar to the curation interface, which has the pair. End is nullable, so
-- a single-residue change is a start with no end -- that satisfies the
-- literal reading too, while leaving room for a lesion spanning residues,
-- which a lone value could only express as free text.
--
-- l_mutated_amino_acids is left in place and stops being read by the form,
-- the same treatment l_location_inline already has. Dropping it would
-- discard whatever curators typed into the old box before this change.

--changeset zirc:zfin-10379-lesion-amino-acid-change

alter table zirc.lesion
    add column if not exists l_aa_change_from     text,
    add column if not exists l_aa_change_to       text,
    add column if not exists l_aa_position_start  integer,
    add column if not exists l_aa_position_end    integer;
