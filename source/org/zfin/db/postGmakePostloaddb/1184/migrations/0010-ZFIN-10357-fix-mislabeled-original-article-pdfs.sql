--liquibase formatted sql

-- ZFIN-10357: repair publication PDFs wrongly marked as 'Original Article'.
--
-- The PMC/EuropePMC acquisition job used to pick an arbitrary PDF from the Open Access
-- package as the main article and additionally mark *every* PDF in the package as
-- 'Original Article' (file type 1). As a result many publications accumulated several
-- type-1 PDF rows when only one is the real article; the rest are supplemental PDFs (and,
-- in some cases, duplicate copies of the article itself). The ingest path has since been
-- fixed to classify the main article by its XML/NXML basename; these changesets repair the
-- historical rows already in the database.
--
-- Only the two unambiguous cases are fixed automatically:
--   A. duplicate rows pointing at the same physical file  -> delete the redundant row(s).
--   B. one genuine main + clearly-named supplement PDFs    -> demote the supplements.
-- Duplicate *original* articles (journal-named auto-load + a curator's ZDB-PUB-named
-- re-upload), companion papers, and unrecognised naming are intentionally left untouched
-- for curator review.

--changeset rtaylor:fix-original-article-delete-duplicate-file-rows
-- A. Collapse redundant 'Original Article' rows that point at the exact same physical file
--    (same publication, same pf_file_name); keep the lowest pf_pk_id. Deleting a row that
--    references an identical file is loss-free.
DELETE FROM publication_file pf
USING (
    SELECT pf_pub_zdb_id, pf_file_name, min(pf_pk_id) AS keep_id
    FROM publication_file
    WHERE pf_file_type_id = (SELECT pft_pk_id FROM publication_file_type WHERE pft_type = 'Original Article')
    GROUP BY pf_pub_zdb_id, pf_file_name
    HAVING count(*) > 1
) dup
WHERE pf.pf_pub_zdb_id = dup.pf_pub_zdb_id
  AND pf.pf_file_name  = dup.pf_file_name
  AND pf.pf_pk_id     <> dup.keep_id
  AND pf.pf_file_type_id = (SELECT pft_pk_id FROM publication_file_type WHERE pft_type = 'Original Article');

--changeset rtaylor:fix-original-article-demote-supplement-pdfs
-- B. Demote supplement-named PDFs to 'Supplemental Material', but only for publications
--    where exactly one non-supplement 'main' row remains, so the sole article is never
--    demoted. The supplement-name pattern matches the common conventions seen in PMC/journal
--    packages (supplement, -SD2, -s001, -sf001/-st001, supp1, .s015, Image_1, .f1, etc.).
UPDATE publication_file pf
SET pf_file_type_id = (SELECT pft_pk_id FROM publication_file_type WHERE pft_type = 'Supplemental Material')
WHERE pf.pf_file_type_id = (SELECT pft_pk_id FROM publication_file_type WHERE pft_type = 'Original Article')
  AND lower(pf.pf_original_file_name) ~ '(supplement|supp[-_ ]?[0-9]|[-_]s[0-9]|[-_]sd[0-9]|[-_]sf[0-9]|[-_]st[0-9]|\.s[0-9]|_figure|_table|_file|figure[ _]s|table[ _]s|image[_ ][0-9]|\.f[0-9]|datas[0-9]|movie|video)'
  AND pf.pf_pub_zdb_id IN (
      SELECT pf_pub_zdb_id
      FROM publication_file
      WHERE pf_file_type_id = (SELECT pft_pk_id FROM publication_file_type WHERE pft_type = 'Original Article')
      GROUP BY pf_pub_zdb_id
      HAVING count(*) > 1
         AND count(*) FILTER (
                WHERE NOT (lower(pf_original_file_name) ~ '(supplement|supp[-_ ]?[0-9]|[-_]s[0-9]|[-_]sd[0-9]|[-_]sf[0-9]|[-_]st[0-9]|\.s[0-9]|_figure|_table|_file|figure[ _]s|table[ _]s|image[_ ][0-9]|\.f[0-9]|datas[0-9]|movie|video)')
             ) = 1
  );
