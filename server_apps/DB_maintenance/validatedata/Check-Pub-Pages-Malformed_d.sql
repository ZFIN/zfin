-- ZFIN-10241: pub_pages values that look like a loader/parse defect.
--
-- Not scheduled in Jenkins.  Run it by hand, for example after changing the PubMed
-- loader or after a large load:
--   psql -d $DBNAME -f $SOURCEROOT/server_apps/DB_maintenance/validatedata/Check-Pub-Pages-Malformed_d.sql
-- (on a dev sidecar: psql -h db -U postgres -d zfindb -f <this file>)
-- No rows means all clear.  Any row is a defect: re-fetch it from PubMed with
-- server_apps/data_transfer/PUBMED/updatePubPages.sh, or add it to the reviewed list
-- once PubMed confirms the value is genuine.
--
-- Every row returned is an error.  Three patterns are reported:
--   concatenated  START + END + START-END   e.g. 535953845359-5384
--   repeated      a value written twice     e.g. 104591104591
--   elsevier_pii  Elsevier PII placeholder  e.g. S0000-0000(26)00000-0
-- The first two came from the old loader reading Pagination.text(); the third is
-- what the loader stored when MedlinePgn was empty.
--
-- Only publications with a PMID are checked: those defects came from the PubMed
-- loader, so a publication without a PMID cannot have them.
--
-- The reviewed list below holds publications that were checked and left as they are.
-- Each entry is a (zdb_id, pub_pages) pair, so the publication is reported again if
-- its pages change to something else.  Add a pair only after checking PubMed.
WITH reviewed (zdb_id, pub_pages) AS (VALUES
  -- PubMed has the same value (genuine repeat)
  ('ZDB-PUB-100309-19', '1717'),
  ('ZDB-PUB-100826-22', '2020'),
  ('ZDB-PUB-180504-16', '1414'),
  ('ZDB-PUB-181115-1', '2929'),
  ('ZDB-PUB-200603-10', '3838'),
  ('ZDB-PUB-201002-102', '110110'),
  ('ZDB-PUB-201203-4', '575575'),
  ('ZDB-PUB-201209-5', '116116'),
  ('ZDB-PUB-210205-10', '632632'),
  ('ZDB-PUB-210603-21', '5353'),
  ('ZDB-PUB-210603-27', '1313'),
  ('ZDB-PUB-211015-1', '4747'),
  ('ZDB-PUB-211025-7', '758758'),
  ('ZDB-PUB-211206-9', '133133'),
  ('ZDB-PUB-211230-9', '3434'),
  ('ZDB-PUB-220503-18', '867867'),
  ('ZDB-PUB-220517-22', '154154'),
  ('ZDB-PUB-220906-23', '1313'),
  ('ZDB-PUB-220906-4', '1010'),
  ('ZDB-PUB-220911-19', '2727'),
  ('ZDB-PUB-220924-5', '2424'),
  ('ZDB-PUB-230312-46', '4747'),
  ('ZDB-PUB-230330-51', '5656'),
  ('ZDB-PUB-230407-53', '5656'),
  ('ZDB-PUB-230414-56', '6767'),
  ('ZDB-PUB-230514-44', '7777'),
  ('ZDB-PUB-231029-62', '7070'),
  ('ZDB-PUB-240620-13', '5757'),
  ('ZDB-PUB-240713-8', '117117'),
  ('ZDB-PUB-240915-21', '9595'),
  ('ZDB-PUB-250515-6', '111111'),
  ('ZDB-PUB-250613-13', '5050'),
  ('ZDB-PUB-260410-11', '4848'),
  ('ZDB-PUB-260414-19', '3131'),
  -- PMID not found in PubMed
  ('ZDB-PUB-240203-8', '3131'),
  ('ZDB-PUB-240203-9', '2727'),
  ('ZDB-PUB-240305-4', '6262'),
  ('ZDB-PUB-240404-1', '8989'),
  ('ZDB-PUB-240605-8', '129129'),
  ('ZDB-PUB-240703-5', '172172'),
  ('ZDB-PUB-240703-6', '172172'),
  ('ZDB-PUB-240802-8', '192192'),
  -- PubMed has no pagination, only the Elsevier PII
  ('ZDB-PUB-260701-7', 'S2451-9456(26)00194-7'),
  ('ZDB-PUB-260716-9', 'S2090-1232(26)00545-X'),
  ('ZDB-PUB-260808-3', 'S0896-6273(26)00571-4'),
  ('ZDB-PUB-260811-2', 'S1673-8527(26)00268-7'),
  ('ZDB-PUB-260815-11', 'S1424-3903(26)00877-X'),
  ('ZDB-PUB-260825-4', 'S2451-9456(26)00288-6'),
  ('ZDB-PUB-260908-4', 'S1538-7836(26)00556-8'),
  ('ZDB-PUB-260910-16', 'S0022-202X(26)02804-6'),
  ('ZDB-PUB-260916-6', 'S0006-3223(26)01521-0'),
  ('ZDB-PUB-260918-12', 'S0006-3495(26)00648-X'),
  ('ZDB-PUB-260929-12', 'S0092-8674(26)01124-4'),
  ('ZDB-PUB-261002-9', 'S2451-9456(26)00330-2'),
  ('ZDB-PUB-261010-10', 'S0012-1606(26)00209-5'),
  ('ZDB-PUB-261010-8', 'S1673-8527(26)00323-1'),
  ('ZDB-PUB-261021-8', 'S0006-3495(26)00719-8'),
  ('ZDB-PUB-261024-10', 'S0960-9822(26)01281-9')
)
SELECT
  p.zdb_id,
  p.accession_no AS pmid,
  j.jrnl_abbrev,
  p.pub_date,
  p.pub_pages,
  CASE
    WHEN p.pub_pages ~ '^S[0-9]{4}-[0-9]{3}[0-9Xx]\(' THEN 'elsevier_pii'
    WHEN p.pub_pages ~ '^([A-Za-z0-9.]+?)([A-Za-z0-9.]+)\1-\2$' THEN 'concatenated'
    ELSE 'repeated'
  END AS problem
FROM publication p
  LEFT JOIN journal j ON j.jrnl_zdb_id = p.pub_jrnl_zdb_id
WHERE p.accession_no IS NOT NULL
  AND p.pub_pages IS NOT NULL
  AND (p.pub_pages ~ '^S[0-9]{4}-[0-9]{3}[0-9Xx]\('
       OR p.pub_pages ~ '^([A-Za-z0-9.]+?)([A-Za-z0-9.]+)\1-\2$'
       OR (length(p.pub_pages) % 2 = 0
           AND length(p.pub_pages) >= 4
           AND substr(p.pub_pages, 1, length(p.pub_pages) / 2) = substr(p.pub_pages, length(p.pub_pages) / 2 + 1)))
  AND (p.zdb_id, p.pub_pages) NOT IN (SELECT zdb_id, pub_pages FROM reviewed)
ORDER BY p.pub_date DESC, p.zdb_id
