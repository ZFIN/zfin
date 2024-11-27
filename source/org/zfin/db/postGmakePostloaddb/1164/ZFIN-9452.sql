--liquibase formatted sql
--changeset cmpich:ZFIN-9452.sql

update source_url set srcurl_url = 'http://www.zfish.cn/resource/'
where srcurl_source_zdb_id = 'ZDB-LAB-130226-1';