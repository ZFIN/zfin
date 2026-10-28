--liquibase formatted sql

-- ZFIN-10241: repair pub_pages values written by the old fetchPubsFromPubMed.groovy, which
-- read article.Pagination.text() and so concatenated every child of <Pagination>:
--
--   START + END + 'START-END'   535953845359-5384   -> 5359-5384    (999 rows)
--   a value written twice       104591104591        -> 104591       (242 rows)
--
-- The second kind was checked against PubMed eFetch: the stored value is two copies of
-- PubMed's <MedlinePgn> (or pii when there is no MedlinePgn). Genuine repeated values such as
-- '4848', where PubMed itself has '4848', are not in the list. The loader no longer produces
-- either form.
--
-- The update job does not repair these, because it only selects publications whose pages or
-- volume are empty or an Elsevier PII placeholder. It does fill those: after this release is
-- deployed, run Jenkins job Update-Pub-Pages_d once with MONTHS=240 (see
-- server_apps/data_transfer/PUBMED/updatePubPages.sh) to backfill empty pub_pages / pub_volume
-- and replace the Elsevier PII placeholders for all years.
--
-- Only written while pub_pages still holds the old value, so rows corrected since the list was
-- made are left alone.

--changeset rtaylor:ZFIN-10241-pub-pages-fix
UPDATE publication p
   SET pub_pages = s.new_pages
  FROM stg_pub_pages_fix s
 WHERE p.zdb_id = s.zdb_id
   AND p.pub_pages = s.old_pages;

--changeset rtaylor:ZFIN-10241-pub-pages-fix-drop-staging
DROP TABLE stg_pub_pages_fix;
