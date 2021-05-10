--liquibase formatted sql
--changeset pm:alter_transcript_types.sql



 update transcript_type set tscriptt_so_id='SO:0000234' where tscriptt_type='mRNA';
 update transcript_type set tscriptt_so_id='SO:0001035' where tscriptt_type='piRNA';
 update transcript_type set tscriptt_so_id='SO:0001463' where tscriptt_type='lincRNA';
 update transcript_type set tscriptt_so_id='SO:0000253' where tscriptt_type='tRNA';
 update transcript_type set tscriptt_so_id='SO:0000252' where tscriptt_type='rRNA';
 update transcript_type set tscriptt_so_id='SO:0001244' where tscriptt_type='pre miRNA';
 update transcript_type set tscriptt_so_id='SO:0000078' where tscriptt_type='polycistronic transcript';

