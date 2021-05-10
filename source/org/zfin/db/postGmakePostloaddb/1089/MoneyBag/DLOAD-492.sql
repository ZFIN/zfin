--liquibase formatted sql
--changeset xiang:DLOAD-492

insert into foreign_db(fdb_db_name,fdb_db_display_name,fdb_db_query,fdb_db_significance) values("UniRule","UniRule","http://prosite.expasy.org/unirule/",12);

