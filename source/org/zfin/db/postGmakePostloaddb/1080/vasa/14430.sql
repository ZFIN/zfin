--liquibase formatted sql
--changeset pkalita:updateGeoFdb

UPDATE foreign_db
SET fdb_db_query = 'http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=', fdb_url_suffix = NULL
WHERE fdb_db_pk_id = 14;
