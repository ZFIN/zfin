--liquibase formatted sql
--changeset patrick:PUB-490

update publication set pub_zebrashare_is_public = 't' where zdb_id = 'ZDB-PUB-181129-1';
