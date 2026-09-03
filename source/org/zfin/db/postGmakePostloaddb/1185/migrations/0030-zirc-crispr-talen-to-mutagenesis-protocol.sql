--liquibase formatted sql

-- Move the CRISPR and TALEN sequences from the lesion to the mutation.
--
-- They were collected under the lesion form's "The insertion is a consequence
-- of" checklist, revealed by its CRISPR / TALEN boxes. Curators decided the
-- sequences belong with the Mutagenesis Protocol instead: the CRISPR sequence
-- shows when the protocol is CRISPR/Cas9, the TALEN sequences when it is
-- TALEN. Protocol is a property of how the line was made, which is recorded
-- once per mutation, so the fields move up with it.
--
-- TALEN gains a second column. TALENs act as a pair and submitters have two
-- sequences to give; the single l_talen_sequence could only hold one.
--
-- The lesion columns are NOT dropped. This repo's habit for a field that
-- leaves the form is to retain its column so previously entered values are
-- not discarded (l_mutated_amino_acids and l_location_inline are the
-- precedents). The copy below is belt-and-braces for the same reason: these
-- tables are empty in dev, but a populated environment must not silently lose
-- what a curator typed.

--changeset zirc:zirc-crispr-talen-to-mutagenesis-protocol

alter table zirc.mutation
    add column if not exists m_crispr_sequence   text,
    add column if not exists m_talen_sequence_1  text,
    add column if not exists m_talen_sequence_2  text;

-- Carry any existing lesion values up to their mutation.
--
-- A mutation has many lesions, so several rows can offer a value for one
-- target column. distinct on (l_mutation_id) ... order by l_id picks the
-- lowest lesion id deterministically rather than letting the outcome depend
-- on scan order. Only non-blank values are candidates, so an empty string on
-- an earlier lesion cannot mask a real sequence on a later one.
--
-- Both updates are guarded with "is null" so re-running this changeset
-- against a table that already has values cannot overwrite them.
update zirc.mutation m
set m_crispr_sequence = src.seq
from (
    select distinct on (l_mutation_id) l_mutation_id, l_crispr_sequence as seq
    from zirc.lesion
    where l_crispr_sequence is not null and btrim(l_crispr_sequence) <> ''
    order by l_mutation_id, l_id
) src
where src.l_mutation_id = m.m_id
  and m.m_crispr_sequence is null;

-- The old single TALEN sequence becomes the first of the pair; the second is
-- left null for the submitter to fill in.
update zirc.mutation m
set m_talen_sequence_1 = src.seq
from (
    select distinct on (l_mutation_id) l_mutation_id, l_talen_sequence as seq
    from zirc.lesion
    where l_talen_sequence is not null and btrim(l_talen_sequence) <> ''
    order by l_mutation_id, l_id
) src
where src.l_mutation_id = m.m_id
  and m.m_talen_sequence_1 is null;
