--liquibase formatted sql

-- ZFIN-10241: staging table for the pub_pages repair in 0040-...-03-post.sql, loaded from
-- csvs/0040-ZFIN-10241-pub-pages-fix.csv by 0040-...-02-loaddata.xml. Permanent rather than
-- TEMP: the load and the UPDATE that reads it are separate changesets (separate
-- transactions), so a TEMP table would not survive between them. Dropped again at the end of
-- 03-post.sql once consumed.

--changeset rtaylor:ZFIN-10241-pub-pages-fix-pre
DROP TABLE IF EXISTS stg_pub_pages_fix;

CREATE TABLE stg_pub_pages_fix (
    zdb_id text,
    old_pages text,
    new_pages text
);
