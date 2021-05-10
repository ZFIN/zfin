--liquibase formatted sql
--changeset pm:ZFIN-6746.sql



 update transcript_type set tscriptt_so_id='SO:0001906' where tscriptt_type='disrupted domain';
 update transcript_type set tscriptt_so_id='SO:0001833' where tscriptt_type='V-gene';
 update transcript_type set tscriptt_so_id='SO:0000101' where tscriptt_type='transposable element';
 update transcript_type set tscriptt_so_id='SO:0000673' where tscriptt_type='transcript';
 update transcript_type set tscriptt_so_id='SO:0000644' where tscriptt_type='antisense';


