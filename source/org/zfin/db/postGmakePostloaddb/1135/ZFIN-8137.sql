--liquibase formatted sql
--changeset cmpich:ZFIN-8137

UPDATE foreign_db SET fdb_db_query = 'http://www.zfishmeta.org/index.php?topic='
WHERE fdb_db_name = 'zfishbook';
