--liquibase formatted sql
--changeset cmpich:ZFIN-9269.sql

update publication set pub_pages = left(pub_pages,8)
where pub_jrnl_zdb_id = 'ZDB-JRNL-151216-1'
  AND LENGTH(pub_pages) = 16
;
