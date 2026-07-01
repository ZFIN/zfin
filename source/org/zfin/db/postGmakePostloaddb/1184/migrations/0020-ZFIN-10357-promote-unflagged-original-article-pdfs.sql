--liquibase formatted sql

-- ZFIN-10357 (follow-up): promote main-article PDFs that were never flagged 'Original Article'.
--
-- The same PMC/EuropePMC acquisition regression that over-labeled supplements (fixed in
-- 0010) also left some publications with the opposite problem: the genuine main article was
-- typed as 'Supplemental Material' or 'Other' and the publication ended up with NO
-- 'Original Article' row at all (e.g. ZDB-PUB-260414-12, ZDB-PUB-180418-36).
--
-- The acquisition pipeline renames the single main article PDF to exactly <zdbId>.pdf
-- (getPDFandImages.groovy recordPdfFromS3), and the curator upload path is the only other
-- writer of pf_file_name -- it gives every non-original file a '<zdbId>-' prefix and reserves
-- the bare '<zdbId>.pdf' name for 'Original Article'. So a publication_file row whose
-- pf_file_name basename is exactly '<zdbId>.pdf' is unambiguously the main article, whatever
-- (wrong) type it currently carries. This changeset promotes those rows.
--
-- Publications whose main article is stored under an irregular name (no '<zdbId>.pdf' row)
-- are intentionally left for curator review -- they cannot be disambiguated from genuinely
-- supplement-only or annotated-only publications.

--changeset rtaylor:fix-original-article-promote-main-named-pdfs
-- Promote the '<zdbId>.pdf' main-article row to 'Original Article', but only for publications
-- that currently have no 'Original Article' row, so we never create a second main article.
UPDATE publication_file pf
SET pf_file_type_id = (SELECT pft_pk_id FROM publication_file_type WHERE pft_type = 'Original Article')
WHERE pf.pf_file_name LIKE '%/' || pf.pf_pub_zdb_id || '.pdf'
  AND pf.pf_file_type_id <> (SELECT pft_pk_id FROM publication_file_type WHERE pft_type = 'Original Article')
  AND NOT EXISTS (
      SELECT 1 FROM publication_file o
      WHERE o.pf_pub_zdb_id = pf.pf_pub_zdb_id
        AND o.pf_file_type_id = (SELECT pft_pk_id FROM publication_file_type WHERE pft_type = 'Original Article')
  );
