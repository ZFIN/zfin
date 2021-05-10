--liquibase formatted sql
--changeset pm:DLOAD-531

update  foreign_db set fdb_db_query="http://omim.org/" where fdb_db_pk_id=24;

