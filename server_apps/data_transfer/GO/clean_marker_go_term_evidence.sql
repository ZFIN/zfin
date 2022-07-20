begin work ;
		 
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

----------------------------------------------------------------------------------------------
---- DELETE duplicates from marker_go_term_evidence and inference_group_member ---------------
---- Part 1.                                                                   ---------------
----------------------------------------------------------------------------------------------
DROP TABLE IF EXISTS tmp_dbg_clean_marker_go_term_evidence1;
DROP TABLE IF EXISTS tmp_to_delete_marker_go_term_evidence1;

-- Create a reference table with marker_go_term_evidence plus hashes and "to_delete" flag
SELECT row_num > 1 as to_delete, * INTO tmp_dbg_clean_marker_go_term_evidence1 FROM
    (
        SELECT
            mgte_hash_full(marker_go_term_evidence) as fullhash,
            mgte_hash_min(marker_go_term_evidence) as minhash,
            row_number() OVER (PARTITION BY mgte_hash_full(marker_go_term_evidence) ORDER BY mrkrgoev_zdb_id) AS row_num,
                marker_go_term_evidence.*
        FROM marker_go_term_evidence
    ) AS subquery;

-- Create a table with ID pairs to track the deleted ones and the replacement IDs (old_mrkrgoev_zdb_id, new_mrkrgoev_zdb_id)
SELECT deletes.mrkrgoev_zdb_id as old_mrkrgoev_zdb_id, keeps.mrkrgoev_zdb_id as new_mrkrgoev_zdb_id
INTO tmp_to_delete_marker_go_term_evidence1 FROM
    (select fullhash, mrkrgoev_zdb_id from tmp_dbg_clean_marker_go_term_evidence1 where to_delete) as deletes
        LEFT JOIN
    (select fullhash, mrkrgoev_zdb_id from tmp_dbg_clean_marker_go_term_evidence1 where not to_delete) as keeps
    ON deletes.fullhash = keeps.fullhash;

-- To prevent data loss in inference_group_member, use the table of old mgte IDs to new ones to update
-- inference_group_member before deleting those IDs from the mgte table
-- since this will result in duplicate entries, temporarily remove constraint and re-add it later
ALTER TABLE inference_group_member DROP CONSTRAINT inference_group_member_primary_key;

UPDATE inference_group_member
    SET infgrmem_mrkrgoev_zdb_id = new_mrkrgoev_zdb_id
    FROM tmp_to_delete_marker_go_term_evidence1
    WHERE infgrmem_mrkrgoev_zdb_id = old_mrkrgoev_zdb_id;

-- delete duplicates
DELETE FROM inference_group_member T1
    USING inference_group_member T2
    WHERE T1.ctid < T2.ctid
    AND T1.infgrmem_mrkrgoev_zdb_id = T2.infgrmem_mrkrgoev_zdb_id
    AND T1.infgrmem_inferred_from = T2.infgrmem_inferred_from;

-- re-add constraint
ALTER TABLE inference_group_member ADD CONSTRAINT inference_group_member_primary_key PRIMARY KEY (infgrmem_mrkrgoev_zdb_id, infgrmem_inferred_from);

-- similar to inference_group_member work above, but for noctua_model_annotation
UPDATE noctua_model_annotation
    SET nma_mrkrgoev_zdb_id = new_mrkrgoev_zdb_id
    FROM tmp_to_delete_marker_go_term_evidence1
    WHERE nma_mrkrgoev_zdb_id = old_mrkrgoev_zdb_id;

-- similar to inference_group_member work above, but for marker_go_term_annotation_extension_group
UPDATE marker_go_term_annotation_extension_group
    SET mgtaeg_mrkrgoev_zdb_id = new_mrkrgoev_zdb_id
    FROM tmp_to_delete_marker_go_term_evidence1
    WHERE mgtaeg_mrkrgoev_zdb_id = old_mrkrgoev_zdb_id;

-- this should be an empty set
select ('The following 4 select statements should be empty');
select * from tmp_to_delete_marker_go_term_evidence1 where old_mrkrgoev_zdb_id is null or new_mrkrgoev_zdb_id is null;
select * from inference_group_member where infgrmem_mrkrgoev_zdb_id in (select old_mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence1);
select * from noctua_model_annotation where nma_mrkrgoev_zdb_id in (select old_mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence1);
select * from marker_go_term_annotation_extension_group where mgtaeg_mrkrgoev_zdb_id in (select old_mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence1);

-- delete duplicates from mgte
DELETE FROM marker_go_term_evidence WHERE mrkrgoev_zdb_id IN (SELECT old_mrkrgoev_zdb_id FROM tmp_to_delete_marker_go_term_evidence1);
----------------------------------------------------------------------------------------------
---- END OF Part 1.                                                            ---------------
----------------------------------------------------------------------------------------------


----------------------------------------------------------------------------------------------
---- DELETE *NEAR* duplicates from marker_go_term_evidence and inference_group_member --------
---- Part 2.                                                                          --------
----------------------------------------------------------------------------------------------
-- Similar to Part 1, except that we are taking into account rows where each column matches
-- except for the protein_accession column, in which case, we delete the rows that contain
-- a null value in the protein_accession column
DROP TABLE IF EXISTS tmp_dbg_clean_marker_go_term_evidence2;
DROP TABLE IF EXISTS tmp_to_delete_marker_go_term_evidence2;

-- Reference table for building up the logic of which rows to delete.
SELECT (row_num > 1 AND mrkrgoev_protein_accession IS NULL) as to_delete,
       *
       INTO tmp_dbg_clean_marker_go_term_evidence2 FROM
    (
        SELECT
            mgte_hash_full(marker_go_term_evidence) as fullhash,
            mgte_hash_min(marker_go_term_evidence) as minhash,
            row_number() OVER (
                PARTITION BY mgte_hash_min(marker_go_term_evidence)
                ORDER BY mrkrgoev_protein_accession, mrkrgoev_zdb_id
            ) AS row_num,
            marker_go_term_evidence.*
        FROM marker_go_term_evidence
    ) AS subquery;

-- Create a table with ID pairs to track the deleted ones and the replacement IDs (old_mrkrgoev_zdb_id, new_mrkrgoev_zdb_id)
SELECT deletes.mrkrgoev_zdb_id as old_mrkrgoev_zdb_id, keeps.mrkrgoev_zdb_id as new_mrkrgoev_zdb_id
INTO tmp_to_delete_marker_go_term_evidence2 FROM
    (select minhash, mrkrgoev_zdb_id from tmp_dbg_clean_marker_go_term_evidence2 where to_delete) as deletes
        LEFT JOIN
    (select minhash, mrkrgoev_zdb_id from tmp_dbg_clean_marker_go_term_evidence2 where not to_delete) as keeps
    ON deletes.minhash = keeps.minhash;

-- To prevent data loss in inference_group_member, use the table of old mgte IDs to new ones to update
-- inference_group_member before deleting those IDs from the mgte table
-- since this will result in duplicate entries, temporarily remove constraint and re-add it later
ALTER TABLE inference_group_member DROP CONSTRAINT inference_group_member_primary_key;

UPDATE inference_group_member
SET infgrmem_mrkrgoev_zdb_id = new_mrkrgoev_zdb_id
    FROM tmp_to_delete_marker_go_term_evidence2
    WHERE infgrmem_mrkrgoev_zdb_id = old_mrkrgoev_zdb_id;

-- delete duplicates
DELETE FROM inference_group_member T1
    USING inference_group_member T2
    WHERE T1.ctid < T2.ctid
      AND T1.infgrmem_mrkrgoev_zdb_id = T2.infgrmem_mrkrgoev_zdb_id
      AND T1.infgrmem_inferred_from = T2.infgrmem_inferred_from;

-- re-add constraint
ALTER TABLE inference_group_member ADD CONSTRAINT inference_group_member_primary_key PRIMARY KEY (infgrmem_mrkrgoev_zdb_id, infgrmem_inferred_from);


-- similar to inference_group_member work above, but for noctua_model_annotation
UPDATE noctua_model_annotation
SET nma_mrkrgoev_zdb_id = new_mrkrgoev_zdb_id
    FROM tmp_to_delete_marker_go_term_evidence2
WHERE nma_mrkrgoev_zdb_id = old_mrkrgoev_zdb_id;

-- similar to inference_group_member work above, but for marker_go_term_annotation_extension_group
UPDATE marker_go_term_annotation_extension_group
SET mgtaeg_mrkrgoev_zdb_id = new_mrkrgoev_zdb_id
    FROM tmp_to_delete_marker_go_term_evidence2
WHERE mgtaeg_mrkrgoev_zdb_id = old_mrkrgoev_zdb_id;

-- this should be an empty set
select ('The following 4 select statements should be empty');
select * from tmp_to_delete_marker_go_term_evidence2 where old_mrkrgoev_zdb_id is null or new_mrkrgoev_zdb_id is null;
select * from inference_group_member where infgrmem_mrkrgoev_zdb_id in (select old_mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence2);
select * from noctua_model_annotation where nma_mrkrgoev_zdb_id in (select old_mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence2);
select * from marker_go_term_annotation_extension_group where mgtaeg_mrkrgoev_zdb_id in (select old_mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence2);

-- delete duplicates from mgte
DELETE FROM marker_go_term_evidence WHERE mrkrgoev_zdb_id IN (SELECT old_mrkrgoev_zdb_id FROM tmp_to_delete_marker_go_term_evidence2);
----------------------------------------------------------------------------------------------
---- END OF Part 2.                                                            ---------------
----------------------------------------------------------------------------------------------


commit work;
