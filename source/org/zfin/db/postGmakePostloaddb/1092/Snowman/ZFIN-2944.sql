--liquibase formatted sql
--changeset pkalita:ZFIN-2944

UPDATE blast_database
SET blastdb_path = 'https://blast.ncbi.nlm.nih.gov/Blast.cgi?PROGRAM=blastn&PAGE_TYPE=BlastSearch'
WHERE blastdb_zdb_id = 'ZDB-BLASTDB-090929-9'
