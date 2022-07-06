--liquibase formatted sql
--changeset rtaylor:ZFIN-7977-2

-- Set the new protein_accession column to the correct value based on table joins
WITH mgte_joined_to_db_link AS (
    SELECT m.mrkrgoev_zdb_id,
           fdb_db_name || ':' ||	dbl.dblink_acc_num as accession
    FROM
        marker_go_term_evidence m,
        db_link dbl,
        foreign_db_contains fdbc,
        foreign_db fdb,
        term
    WHERE dblink_zdb_id IS NOT NULL AND mrkrgoev_protein_accession IS NULL
      AND mrkrgoev_protein_dblink_zdb_id = dbl.dblink_zdb_id
      AND dbl.dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
      AND fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
      AND mrkrgoev_term_zdb_id = term.term_zdb_id
      AND NOT term_is_obsolete
      AND NOT term_is_secondary
)
UPDATE marker_go_term_evidence mgte
    SET mrkrgoev_protein_accession = mgte_joined_to_db_link.accession
    FROM mgte_joined_to_db_link
    WHERE mgte.mrkrgoev_zdb_id = mgte_joined_to_db_link.mrkrgoev_zdb_id;

DROP TABLE IF EXISTS tmp_to_delete_marker_go_term_evidence;

-- DELETE duplicates from marker_go_term_evidence (matches rows with same fields listed in partition clause)
-- this will match exact duplicates in the mrkrgoev_protein_accession field (among other fields)
SELECT * INTO tmp_to_delete_marker_go_term_evidence FROM marker_go_term_evidence WHERE mrkrgoev_zdb_id IN (
    SELECT mrkrgoev_zdb_id
    FROM (
        SELECT
            mrkrgoev_zdb_id,
            row_number() OVER (PARTITION BY
                mrkrgoev_mrkr_zdb_id,
                mrkrgoev_source_zdb_id,
                mrkrgoev_evidence_code,
                mrkrgoev_notes,
                mrkrgoev_contributed_by,
                mrkrgoev_modified_by,
                mrkrgoev_gflag_name,
                mrkrgoev_term_zdb_id,
                mrkrgoev_annotation_organization,
                mrkrgoev_annotation_organization_created_by,
                mrkrgoev_relation_term_zdb_id,
                mrkrgoev_relation_qualifier,
                mrkrgoev_tag_submit_format,
                mrkrgoev_protein_accession
                ORDER BY mrkrgoev_zdb_id) AS row_num
        FROM "public"."marker_go_term_evidence") AS subquery
    WHERE row_num > 1);


-- Similar to above, but need to handle case of nulls for accession
-- so this will delete any duplicates where each field matches, but one row contains null protein_accession
INSERT INTO tmp_to_delete_marker_go_term_evidence
SELECT * FROM marker_go_term_evidence
WHERE
        mrkrgoev_zdb_id NOT IN ( select mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence )
  AND
        mrkrgoev_zdb_id IN (
        SELECT mrkrgoev_zdb_id
        FROM (
                 SELECT
                     mrkrgoev_zdb_id,
                     mrkrgoev_protein_accession,
                     row_number() OVER (PARTITION BY
                    mrkrgoev_mrkr_zdb_id,
                    mrkrgoev_source_zdb_id,
                    mrkrgoev_evidence_code,
                    mrkrgoev_notes,
                    mrkrgoev_contributed_by,
                    mrkrgoev_modified_by,
                    mrkrgoev_gflag_name,
                    mrkrgoev_term_zdb_id,
                    mrkrgoev_annotation_organization,
                    mrkrgoev_annotation_organization_created_by,
                    mrkrgoev_relation_term_zdb_id,
                    mrkrgoev_relation_qualifier,
                    mrkrgoev_tag_submit_format
                    ORDER BY mrkrgoev_protein_accession, mrkrgoev_zdb_id) AS row_num
                 FROM "public"."marker_go_term_evidence") AS subquery
        WHERE row_num > 1
          AND mrkrgoev_protein_accession IS NULL );

-- ==== find out if other DB tables depend on the rows to delete =====
select * from inference_group_member where infgrmem_mrkrgoev_zdb_id in (select mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence);
select * from noctua_model_annotation where nma_mrkrgoev_zdb_id in (select mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence);
select * from marker_go_term_annotation_extension_group where mgtaeg_mrkrgoev_zdb_id in (select mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence);

-- ===== do the delete =======
DELETE FROM marker_go_term_evidence where mrkrgoev_zdb_id in
      (select mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence);
