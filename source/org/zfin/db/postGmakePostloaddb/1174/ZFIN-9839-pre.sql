--liquibase formatted sql
--changeset cmpich:ZFIN-9839-pre

create  table temp_zmp (
    allele text,
    assembly    text,
    chromosome text,
    position    text
);
