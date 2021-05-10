--liquibase formatted sql
--changeset pm:transcript_sequence.sql

create table transcript_sequence (ts_transcript_zdb_id text not null primary key,
                               ts_transcript_ottdart_id text not null,
                               ts_sequence text)
                              
;

alter table transcript_sequence
 add constraint transcript_sequence_transcript_foreign_key
foreign key (ts_transcript_zdb_id) references transcript(tscript_mrkr_zdb_id)
on delete cascade;


