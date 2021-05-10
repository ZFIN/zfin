-- loadTranscriptSequences.sql

begin work;
create  table tscriptsequence (tscriptid text, tscriptottdart text, tscriptseq text );

\copy tscriptsequence from '<!--|ROOT_PATH|-->/server_apps/data_transfer/RNACentral/loadedSeq.txt' delimiter E'\t';
insert into transcript_sequence (select tscriptid, tscriptottdart, substring(tscriptseq from 2 for length(tscriptseq))  from tscriptsequence where tscriptseq!='') ;

commit work;






