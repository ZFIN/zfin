--liquibase formatted sql
--changeset rtaylor:zfin-8396

update zdb_submitters set password = ''
where length(password) = 32 and regexp_match(password, '^[0-9a-f]{32}$') is not null; --matches md5 hashes

