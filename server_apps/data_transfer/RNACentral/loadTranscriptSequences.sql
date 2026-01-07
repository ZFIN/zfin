-- loadTranscriptSequences.sql

begin work;
drop table if exists tscriptsequence;
create table tscriptsequence
(
    tscriptid      text,
    tscriptottdart text,
    tscriptseq     text
);

\copy tscriptsequence from './loadedSeq.txt' delimiter E'\t';

delete from transcript_sequence;

insert into transcript_sequence (ts_transcript_zdb_id, ts_transcript_ottdart_id, ts_sequence) (select tscriptid, tscriptottdart, substring(tscriptseq from 2 for length(tscriptseq))  from tscriptsequence where tscriptseq!='') ;

drop table tscriptsequence;

commit work;






