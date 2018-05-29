--liquibase formatted sql
--changeset kschaper:ZFIN-5934.sql

update foreign_db set fdb_db_name = 'Ensembl(GRCz11)' where fdb_db_name = 'Ensembl(GRCz10)';
