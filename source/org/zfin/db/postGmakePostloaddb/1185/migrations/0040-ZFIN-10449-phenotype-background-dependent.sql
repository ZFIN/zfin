--liquibase formatted sql

-- ZFIN-10449: a new question at the bottom of the phenotype form, "Is
-- phenotype background dependent", answered yes/no; picking yes reveals a
-- free-text box for the details.
--
-- Two columns rather than one, and the boolean is nullable, because the form
-- has three states a submitter can be in and they are not the same thing:
-- unanswered (NULL), answered no (false), answered yes (true). Encoding "no"
-- and "not yet asked" as the same value would leave the status badge unable
-- to tell a finished phenotype from an untouched one -- the same reason
-- p_zfin_image_permission and p_zirc_image_permission are nullable booleans.
--
-- The text is only meaningful when the answer is yes, and the form hides it
-- otherwise. Nothing here enforces that pairing: a submitter who answers yes,
-- types something and then switches to no leaves the text behind. That is
-- deliberate -- switching back should not silently destroy what they wrote.
-- The form reads it only under yes, so a stranded value is invisible rather
-- than wrong.
--
-- Mirrors p_non_mendelian_comment, which is the same reveal-on-answer shape
-- driven by the segregation dropdown.
--
-- Note on wording: no line of this preamble may begin with "comment" after
-- the dashes. Liquibase's formatted-SQL parser reads "-- comment ..." as its
-- --comment directive, which is only legal inside a changeset, and the file
-- fails to parse with "comment lines outside of changesets".

--changeset cmpich:ZFIN-10449-phenotype-background-dependent
ALTER TABLE zirc.phenotype
    ADD COLUMN IF NOT EXISTS p_background_dependent BOOLEAN;
ALTER TABLE zirc.phenotype
    ADD COLUMN IF NOT EXISTS p_background_comment TEXT;
--rollback ALTER TABLE zirc.phenotype DROP COLUMN IF EXISTS p_background_dependent;
--rollback ALTER TABLE zirc.phenotype DROP COLUMN IF EXISTS p_background_comment;
