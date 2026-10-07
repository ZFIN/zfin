--liquibase formatted sql

-- ZFIN-10545: the PMID foreign_db entry still pointed at the old NCBI Entrez
-- query.fcgi endpoint; point it at the current PubMed URL instead.

--changeset rtaylor:ZFIN-10545-pubmed-foreign-db-query-url
UPDATE foreign_db
SET fdb_db_query = 'https://pubmed.ncbi.nlm.nih.gov/'
WHERE fdb_db_name = 'PMID'
  AND fdb_db_query = 'http://www.ncbi.nlm.nih.gov:80/entrez/query.fcgi?cmd=search&db=PubMed&dopt=Abstract&term=';
--rollback UPDATE foreign_db SET fdb_db_query = 'http://www.ncbi.nlm.nih.gov:80/entrez/query.fcgi?cmd=search&db=PubMed&dopt=Abstract&term=' WHERE fdb_db_name = 'PMID';
