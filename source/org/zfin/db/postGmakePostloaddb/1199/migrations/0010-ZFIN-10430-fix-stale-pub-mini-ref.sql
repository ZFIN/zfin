--liquibase formatted sql
--changeset rtaylor:ZFIN-10430-fix-stale-pub-mini-ref

-- pub_mini_ref is computed by the publication INSERT/UPDATE triggers via
-- get_pub_mini_ref(), which re-queried the publication table by zdb_id
-- instead of using the row being written. A BEFORE trigger's self-SELECT
-- sees the pre-statement row, so any single-statement UPDATE that corrected
-- a mis-encoded authors value left pub_mini_ref computed from the old, bad
-- authors text -- freezing the corruption into pub_mini_ref even after
-- authors itself was fixed. This trigger bug is fixed in the same commit
-- (lib/DB_triggers/publication.sql, publication_update.sql,
-- lib/DB_functions/get_pub_mini_ref.sql).
--
-- Two publications were left with a Unicode replacement character
-- (U+FFFD) in pub_mini_ref where authors has extended Latin characters:
-- ZDB-PUB-080924-1 (Sundström) and ZDB-PUB-250313-2 (Luxán). authors is
-- correct for both rows, so recomputing pub_mini_ref reproduces the
-- correct citation.
update publication
   set pub_mini_ref = get_pub_mini_ref(zdb_id)
 where zdb_id in ('ZDB-PUB-080924-1', 'ZDB-PUB-250313-2');
