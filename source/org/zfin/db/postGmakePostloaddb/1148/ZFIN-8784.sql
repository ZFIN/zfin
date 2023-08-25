--liquibase formatted sql
--changeset cmpich:ZFIN-8784.sql

update marker_sequence
set seq_sequence = upper(seq_sequence)
where seq_sequence != upper(seq_sequence);