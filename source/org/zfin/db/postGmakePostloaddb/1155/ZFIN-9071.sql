--liquibase formatted sql
--changeset cmpich:ZFIN-9071.sql

delete from pub_db_xref where pdx_pub_zdb_id = 'ZDB-PUB-240117-5';
