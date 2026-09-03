--liquibase formatted sql

-- ZFIN-10403b: the insertion-origin checklist keeps only CRISPR and TALEN.
--
-- "Construct or other species DNA", "Other" and "Unknown" are not wanted.
-- The first two were the only readers of l_construct_name and
-- l_insertion_origin_other, so with the boxes gone both columns are
-- unreachable from the form and nothing else reads them.
--
-- Dropped rather than left in place for the same reason 0060 dropped the two
-- boolean columns: they were added in this same unreleased batch (0030 and
-- 0060 of ZFIN-10400) and hold no data outside development, so there is
-- nothing to preserve and leaving them would be two dead columns.
--
-- l_crispr_sequence and l_talen_sequence stay -- their boxes stay.
--
-- The surviving token list is not constrained here. It is enforced by the
-- form's option list, the same as before; a check constraint would have to be
-- revised by migration every time curators revise the checklist.
--
-- A separate changeset from 0030/0060 because those have already run --
-- editing them would change their checksums.

--changeset zirc:zfin-10403b-drop-unused-insertion-origins

alter table zirc.lesion
    drop column if exists l_insertion_origin_other,
    drop column if exists l_construct_name;

-- Clear the retired tokens from rows that already carry them, so a
-- development row cannot keep claiming an origin the form can no longer
-- show, edit or clear.
update zirc.lesion
   set l_insertion_origins = array(
           select t from unnest(l_insertion_origins) as t
            where t in ('crispr', 'talen'))
 where l_insertion_origins && array['construct', 'other', 'unknown']::text[];
