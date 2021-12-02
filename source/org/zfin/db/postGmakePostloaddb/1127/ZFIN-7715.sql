--liquibase formatted sql
--changeset cmpich:ZFIN-7715

-- update signafish URL
update foreign_db set fdb_db_query = 'http://signalink.org/protein/'
where fdb_db_name = 'SignaFish';

