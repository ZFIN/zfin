--liquibase formatted sql
--changeset rtaylor:ZFIN-8154

-- Revert 8115

update foreign_db set fdb_db_query = 'http://www.uniprot.org/uniprot/'
where fdb_db_query = 'https://rest.uniprot.org/uniprotkb/'
  and fdb_db_pk_id = 40;
