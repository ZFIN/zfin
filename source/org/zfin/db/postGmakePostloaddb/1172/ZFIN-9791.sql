--liquibase formatted sql
--changeset rtaylor:ZFIN-9791

-- Add non-null constraint to marker_sequence.seq_sequence
alter table marker_sequence
    alter column seq_sequence set not null;