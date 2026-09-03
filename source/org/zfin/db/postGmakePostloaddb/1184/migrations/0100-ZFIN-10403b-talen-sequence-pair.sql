--liquibase formatted sql

-- ZFIN-10403b: a TALEN is a pair, so the form collects two sequences.
--
-- TALENs cut as two arms flanking the target site; one sequence box was
-- asking for half an answer. l_talen_sequence becomes l_talen_sequence_1 and
-- a second column joins it, rather than one array column: the count is fixed
-- at two and each half is labelled separately in the form.
--
-- Renamed rather than dropped and recreated so anything already typed into
-- the box in development lands in arm 1, where a curator would have put it.
--
-- l_crispr_sequence is untouched -- CRISPR really is one sequence.
--
-- A separate changeset from 0030 of ZFIN-10400, which added the original
-- column: that one has already run, and editing it would change its checksum.

-- The rename is guarded because dev databases are at mixed states: some have
-- the pre-rename column, some (rebuilt after this migration) never had it.
-- splitStatements:false because the plpgsql body's own semicolons are not
-- statement boundaries -- Liquibase's splitter cuts the block mid-body and
-- Postgres rejects the fragment as an unterminated dollar quote.

--changeset zirc:zfin-10403b-talen-sequence-rename splitStatements:false

do $$
begin
    if exists (select 1
                 from information_schema.columns
                where table_schema = 'zirc'
                  and table_name   = 'lesion'
                  and column_name  = 'l_talen_sequence') then
        alter table zirc.lesion rename column l_talen_sequence to l_talen_sequence_1;
    end if;
end $$;

-- Separate changeset: one statement, so it needs no splitter exemption.
-- add-if-not-exists covers arm 1 on a database that never had the old column.

--changeset zirc:zfin-10403b-talen-sequence-pair

alter table zirc.lesion
    add column if not exists l_talen_sequence_1 text,
    add column if not exists l_talen_sequence_2 text;
