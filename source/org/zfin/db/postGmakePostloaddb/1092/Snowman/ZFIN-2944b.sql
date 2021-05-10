--liquibase formatted sql
--changeset pkalita:ZFIN-2944b

UPDATE blast_database
SET blastdb_path = 'https://blast.ncbi.nlm.nih.gov/Blast.cgi??DATABASE=nr&PAGE=MegaBlast&FILTER=L&QUERY='
WHERE blastdb_zdb_id = 'ZDB-BLASTDB-090929-9'
