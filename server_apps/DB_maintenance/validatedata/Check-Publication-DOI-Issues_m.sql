-- ZFIN-10394: publications whose pub_doi needs attention.
--
-- Two categories, both reported here because both block the eventual uniqueness
-- constraint on this column:
--
--   DUPLICATE DOI   two or more publications share a DOI (compared
--                   case-insensitively and ignoring resolver prefixes, since DOI
--                   names are case-insensitive per the DOI Handbook).
--   MALFORMED DOI   the value is not a DOI once resolver prefixes are stripped.
--                   Every DOI begins "10." followed by the registrant code.
--
-- Duplicates are NOT all the same problem and must be adjudicated individually --
-- as of 2026-09 the four groups are, in order: an epub/final pair where one record
-- has no PMID; an epub/final pair differing only by a hyphen in the title; the same
-- article listed under both "Gene X" and "Gene"; and an article plus its
-- "Correction:" notice, which is a genuinely separate publication that should carry
-- its own DOI rather than repeating the original's. Only the first two are
-- straightforward merges. The columns below (PMID, date, journal, title) are here
-- so a curator can tell those cases apart without opening each record.
--
-- Ancestry of this report: ZFIN-10358 added DOI resolution to the GO loads, where a
-- DOI matching more than one publication is rejected outright, so every duplicate
-- here is a citation the GO loads cannot use.

WITH normalized AS (
    SELECT zdb_id,
           pub_doi,
           -- strip resolver prefix / doi: scheme, applied twice: a few rows carry a
           -- doubled prefix such as "https://doi.org/doi:10.25335/e4ts-4572"
           regexp_replace(
             regexp_replace(btrim(pub_doi),
                            '^(https?://)?(dx[.])?doi[.]org/|^doi:[[:space:]]*', '', 'i'),
                            '^(https?://)?(dx[.])?doi[.]org/|^doi:[[:space:]]*', '', 'i') AS norm_doi
      FROM publication
     WHERE pub_doi IS NOT NULL
       AND btrim(pub_doi) <> ''
),
duplicates AS (
    SELECT lower(norm_doi) AS key
      FROM normalized
     GROUP BY lower(norm_doi)
    HAVING count(*) > 1
)
SELECT CASE WHEN n.norm_doi !~ '^10[.]' THEN 'MALFORMED DOI' ELSE 'DUPLICATE DOI' END AS issue_type,
       n.zdb_id,
       n.pub_doi                                   AS stored_value,
       n.norm_doi                                  AS normalized_value,
       p.accession_no                              AS pmid,
       p.pub_date,
       j.jrnl_abbrev                               AS journal,
       p.title,
       CASE
         WHEN n.norm_doi !~ '^10[.]'
           THEN 'not a DOI after stripping prefixes; a DOI begins "10."'
         ELSE 'shares this DOI with: ' ||
              (SELECT string_agg(o.zdb_id, ', ' ORDER BY o.zdb_id)
                 FROM normalized o
                WHERE lower(o.norm_doi) = lower(n.norm_doi)
                  AND o.zdb_id <> n.zdb_id)
       END                                         AS detail
  FROM normalized n
  JOIN publication p ON p.zdb_id = n.zdb_id
  LEFT JOIN journal j ON j.jrnl_zdb_id = p.pub_jrnl_zdb_id
 WHERE n.norm_doi !~ '^10[.]'
    OR lower(n.norm_doi) IN (SELECT key FROM duplicates)
 ORDER BY issue_type, lower(n.norm_doi), n.zdb_id;
