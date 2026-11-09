--liquibase formatted sql

-- ZFIN-10241: staging table for the second pub_pages repair, 0041-...-03-post.sql, loaded from
-- csvs/0041-ZFIN-10241-pub-pages-fix-b.csv by 0041-...-02-loaddata.xml. Permanent rather than
-- TEMP for the same reason as in 0040: load and UPDATE are separate changesets.

--changeset rtaylor:ZFIN-10241-pub-pages-fix-b-pre
DROP TABLE IF EXISTS stg_pub_pages_fix_b;

CREATE TABLE stg_pub_pages_fix_b (
    zdb_id text,
    old_pages text,
    new_pages text
);
