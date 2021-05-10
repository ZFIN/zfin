--liquibase formatted sql
--changeset pkalita:ZFIN-5818

DELETE
FROM blast_database
WHERE blastdb_zdb_id IN (
  'ZDB-BLASTDB-090929-8',
  'ZDB-BLASTDB-110224-1',
  'ZDB-BLASTDB-130401-1'
);
