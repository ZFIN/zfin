--liquibase formatted sql
--changeset rtaylor:ZFIN-10613-lncrna-transcript-type

-- ZFIN-10613: Ensembl labels about 40,000 transcripts per assembly lncRNA, and ZFIN had no transcript type for them.
-- Added alongside lincRNA (same display convention, same order, indented under ncRNA); SO:0001877 is lncRNA, whose
-- name and definition (term table) are reused here.

insert into transcript_type
  (tscriptt_type, tscriptt_display, tscriptt_definition, tscriptt_order, tscriptt_indent, tscriptt_so_id)
select 'lncRNA', 'lncRNA', term_definition, 3, true, term_ont_id
  from term
 where term_ont_id = 'SO:0001877'
   and not exists (select 1 from transcript_type where tscriptt_type = 'lncRNA');
