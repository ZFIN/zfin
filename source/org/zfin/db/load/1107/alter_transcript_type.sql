--liquibase formatted sql
--changeset pm:alter_transcript_type.sql

alter table transcript_type
 add tscriptt_so_id text;

 update transcript_type set tscriptt_so_id='SO:0000655' where tscriptt_type='ncRNA';
 update transcript_type set tscriptt_so_id='SO:0000644' where tscriptt_type='antisense ';
 update transcript_type set tscriptt_so_id='SO:0001503' where tscriptt_type='aberrant processed transcript';
 update transcript_type set tscriptt_so_id='SO:0002111' where tscriptt_type='pseudogenic transcript';
 update transcript_type set tscriptt_so_id='SO:0000276' where tscriptt_type='miRNA';
 update transcript_type set tscriptt_so_id='SO:0000275' where tscriptt_type='snoRNA';
 update transcript_type set tscriptt_so_id='SO:0000274' where tscriptt_type='snRNA';
 update transcript_type set tscriptt_so_id='SO:0000013' where tscriptt_type='scRNA';
