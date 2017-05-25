--liquibase formatted sql
--changeset Christian:ONT-618

insert into foreign_db (fdb_db_Name, fdb_db_query, fdb_db_display_name, fdb_db_Significance)
values ('HTTPS', '', 'Https://',29)
