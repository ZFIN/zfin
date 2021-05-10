--liquibase formatted sql
--changeset pm:DLOAD-552_pre

create table  sanger_flanking_sequence (
 allele text not null,
        ref text not null,
           alt text not null,
           strand text not null,
            flankingseq text not null) ;

