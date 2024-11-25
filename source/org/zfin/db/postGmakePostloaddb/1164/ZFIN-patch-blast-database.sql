--liquibase formatted sql
--changeset cmpich:ZFIN-patch.sql

insert into blast_database
select get_id('BLASTDB'), 'Ensembl GRCz11 ZFIN Transcripts', 'ensembl_zf_only', 'Ensembl Transcripts for Zebrafish',
       'f','nucleotide',null,'Ensembl GRCz11 ZFIN Transcripts',null,'f',bdot_pk_id,null,null,null,null from blast_database_origination_type
where bdot_type = 'CURATED';
