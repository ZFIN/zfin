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
---- DELETE *NEAR* duplicates from marker_go_term_evidence and inference_group_member --------
----                                                                                  --------
----------------------------------------------------------------------------------------------
-- Delete rows from marker_go_term_evidence that are duplicates of other rows in the same table
-- except that the other row contains a protein accession (i.e. isoform).
-- In other words, we are taking into account rows where each column matches (minus metadata)
-- except for the protein_accession column, in which case, we delete the rows that contain
-- a null value in the protein_accession column.  Any rows that we delete, we also update 
-- the inference_group_member table to reflect the change. 
DROP TABLE IF EXISTS tmp_dbg_clean_marker_go_term_evidence;
DROP TABLE IF EXISTS tmp_to_delete_marker_go_term_evidence;
DROP TABLE IF EXISTS tmp_inference_group_member_updates;

-- Reference table for building up the logic of which rows to delete.
-- SELECT (row_num > 1 AND mrkrgoev_protein_accession IS NULL) as to_delete,
SELECT
    mgte_hash_full(marker_go_term_evidence) as fullhash,
    mgte_hash_min(marker_go_term_evidence) as minhash,
    row_number() OVER (
      PARTITION BY mgte_hash_min(marker_go_term_evidence)
      ORDER BY mrkrgoev_protein_accession, mrkrgoev_zdb_id
    ) AS row_num,
    marker_go_term_evidence.*
  INTO tmp_dbg_clean_marker_go_term_evidence
  FROM marker_go_term_evidence;

-- Create a table with ID pairs to track the deleted ones and the replacement IDs (old_mrkrgoev_zdb_id, new_mrkrgoev_zdb_id)
SELECT
    t1.mrkrgoev_zdb_id AS old_mrkrgoev_zdb_id,
    t2.mrkrgoev_zdb_id AS new_mrkrgoev_zdb_id
  INTO tmp_to_delete_marker_go_term_evidence
  FROM
    tmp_dbg_clean_marker_go_term_evidence t1,
    tmp_dbg_clean_marker_go_term_evidence t2
  WHERE
    t1.minhash = t2.minhash
    AND t1.mrkrgoev_protein_accession IS NULL
    AND t2.mrkrgoev_protein_accession IS NOT NULL;

-- since there may be multiple entries now in the to_delete table that map an old_mrkrgoev_zdb_id to different
-- new_mrkrgoev_zdb_ids, we need to figure out which one to keep.  It is arbitrary which one we keep, but we
-- can just sort by new_id to be deterministic.
DELETE FROM tmp_to_delete_marker_go_term_evidence T1
    USING tmp_to_delete_marker_go_term_evidence T2
WHERE T1.new_mrkrgoev_zdb_id > T2.new_mrkrgoev_zdb_id
  AND T1.old_mrkrgoev_zdb_id = T2.old_mrkrgoev_zdb_id;

SELECT inference_group_member.*, new_mrkrgoev_zdb_id
  INTO tmp_inference_group_member_updates
  FROM inference_group_member, tmp_to_delete_marker_go_term_evidence
  WHERE infgrmem_mrkrgoev_zdb_id = old_mrkrgoev_zdb_id;

-- To prevent data loss in inference_group_member, use the table of old mgte IDs to new ones to update
-- inference_group_member before deleting those IDs from the mgte table
-- since this will result in duplicate entries, temporarily remove constraint and re-add it later
ALTER TABLE inference_group_member DROP CONSTRAINT inference_group_member_primary_key;

UPDATE inference_group_member
SET infgrmem_mrkrgoev_zdb_id = new_mrkrgoev_zdb_id
    FROM tmp_to_delete_marker_go_term_evidence
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
    FROM tmp_to_delete_marker_go_term_evidence
WHERE nma_mrkrgoev_zdb_id = old_mrkrgoev_zdb_id;

-- similar to inference_group_member work above, but for marker_go_term_annotation_extension_group
UPDATE marker_go_term_annotation_extension_group
SET mgtaeg_mrkrgoev_zdb_id = new_mrkrgoev_zdb_id
    FROM tmp_to_delete_marker_go_term_evidence
WHERE mgtaeg_mrkrgoev_zdb_id = old_mrkrgoev_zdb_id;

-- this should be an empty set
select ('The following 4 select statements should be empty');
select * from tmp_to_delete_marker_go_term_evidence where old_mrkrgoev_zdb_id is null or new_mrkrgoev_zdb_id is null;
select * from inference_group_member where infgrmem_mrkrgoev_zdb_id in (select old_mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence);
select * from noctua_model_annotation where nma_mrkrgoev_zdb_id in (select old_mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence);
select * from marker_go_term_annotation_extension_group where mgtaeg_mrkrgoev_zdb_id in (select old_mrkrgoev_zdb_id from tmp_to_delete_marker_go_term_evidence);

-- delete duplicates from mgte
DELETE FROM marker_go_term_evidence WHERE mrkrgoev_zdb_id IN (SELECT old_mrkrgoev_zdb_id FROM tmp_to_delete_marker_go_term_evidence);
----------------------------------------------------------------------------------------------
---- END OF Part 2.                                                            ---------------
----------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------
---- Part 3. Full duplicates in marker_go_term_evidence                        ---------------
----------------------------------------------------------------------------------------------
-- Identify any full duplicates in marker_go_term_evidence + inference_group_member
-- i.e. rows where every column matches (minus metadata columns of date_entered and data_modified)
-- We should keep one row of each set of duplicates, but we need to identify them first

drop table if exists tmp_mgte_duplicates;

create temp table tmp_mgte_duplicates as
select '' as _hash, * from marker_go_term_evidence join inference_group_member on mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id;

update tmp_mgte_duplicates set _hash =
                                   md5(row(
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
                                       mrkrgoev_external_load_date,
                                       mrkrgoev_protein_dblink_zdb_id,
                                       mrkrgoev_relation_term_zdb_id,
                                       mrkrgoev_relation_qualifier,
                                       mrkrgoev_tag_submit_format,
                                       mrkrgoev_protein_accession,
                                       infgrmem_inferred_from,
                                       infgrmem_notes)::text);


delete from tmp_mgte_duplicates where _hash not in (
    select _hash from tmp_mgte_duplicates group by _hash having count(*) > 1
);

-- Now we have a table of only duplicates.  We need to delete all but one of each set of duplicates.
-- We can keep the row with the min value for mrkrgoev_zdb_id
-- Let's start with inference_group_member
delete from inference_group_member
    where infgrmem_mrkrgoev_zdb_id in (
        select mrkrgoev_zdb_id from tmp_mgte_duplicates tmd
        where mrkrgoev_zdb_id not in (
            select min(mrkrgoev_zdb_id) from tmp_mgte_duplicates tmd2
            where tmd._hash = tmd2._hash
            group by _hash
        )
    );

-- Now delete from marker_go_term_evidence (this time we can use the ID instead of the compound key)
delete from marker_go_term_evidence
where mrkrgoev_zdb_id in (
    select mrkrgoev_zdb_id from tmp_mgte_duplicates tmd
    where mrkrgoev_zdb_id not in (
        select min(mrkrgoev_zdb_id) from tmp_mgte_duplicates tmd2
        where tmd._hash = tmd2._hash
        group by _hash
    )
);

-- Export the results to CSV files for record-keeping
select 'Copying log files to: clean_marker_go_term_evidence.csv, to_delete_marker_go_term_evidence.csv, tmp_inference_group_member_updates.csv';
\copy (select * from tmp_dbg_clean_marker_go_term_evidence) to 'clean_marker_go_term_evidence.csv' with csv header;
\copy (select * from tmp_to_delete_marker_go_term_evidence) to 'to_delete_marker_go_term_evidence.csv' with csv header;
\copy (select * from tmp_inference_group_member_updates) to 'tmp_inference_group_member_updates.csv' with csv header;
\copy (select * from tmp_mgte_duplicates order by _hash) to 'tmp_mgte_duplicates.csv' with csv header;

-- Clean up temporary tables
DROP TABLE IF EXISTS tmp_dbg_clean_marker_go_term_evidence;
DROP TABLE IF EXISTS tmp_to_delete_marker_go_term_evidence;
DROP TABLE IF EXISTS tmp_inference_group_member_updates;
DROP TABLE IF EXISTS tmp_mgte_duplicates;


commit work;
