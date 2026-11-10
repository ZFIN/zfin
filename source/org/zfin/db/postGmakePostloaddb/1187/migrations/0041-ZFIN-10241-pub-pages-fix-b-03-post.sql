--liquibase formatted sql

-- ZFIN-10241: second pub_pages repair, from review of the suspicious-pages report that followed
-- 0040. Each value was checked against PubMed eFetch (MedlinePgn) or, for
-- publications PubMed does not have, Crossref (by DOI, or by title for ZDB-PUB-010718-43):
--
--   end before start   5953-5862 (PubMed 5853-62)               11 rows
--   value doubled      e107621e107621 -> e107621, 3131 -> 31    21 rows with no PMID, or a PMID that
--                      PubMed no longer serves (Lab Anim (NY)); Crossref has a single page
--                      or article number ('31-31', 'e107621'). 0040 and the 1182 dedup only
--                      repaired publications whose pages PubMed confirmed.
--
-- ZDB-PUB-260530-25, which had its DOI written twice in pages, is repaired by 0040 (PubMed
-- 125-126); only its missing volume is filled here.
--
-- Only written while pub_pages still holds the old value, so rows corrected since the list was
-- made are left alone.

--changeset rtaylor:ZFIN-10241-pub-pages-fix-b
UPDATE publication p
   SET pub_pages = s.new_pages
  FROM stg_pub_pages_fix_b s
 WHERE p.zdb_id = s.zdb_id
   AND p.pub_pages = s.old_pages;

--changeset rtaylor:ZFIN-10241-pub-pages-fix-b-volume
-- ZDB-PUB-260530-25 was stored without a volume; PubMed has volume 23, issue 4-5. Written here
-- rather than left to Update-Pub-Pages_d, which would store the volume without the issue.
UPDATE publication
   SET pub_volume = '23(4-5)'
 WHERE zdb_id = 'ZDB-PUB-260530-25'
   AND (pub_volume IS NULL OR pub_volume = '');

--changeset rtaylor:ZFIN-10241-pub-pages-fix-b-drop-staging
DROP TABLE stg_pub_pages_fix_b;
