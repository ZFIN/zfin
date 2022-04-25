--liquibase formatted sql
--changeset cmpich:ZFIN-7974

update zdb_submitters set is_curator = 'f' where login in ('jknight264','storo');