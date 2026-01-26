-- loadNCBIgeneAccs.sql
--
-- This SQL script deletes the db_link records according to the delete list, toDelete.unl.
-- And it loads all the following kinds of db_link records according to the add list, toLoad.unl.
-- 1) NCBI Gene Ids
-- 2) RefSeq accessioons (including RefSeq RNA, RefPept, RefSeq DNA)
-- 3) GenBank accessions (including GenBank RNA, GenPept, GenBank DNA)
-- The script also attribute the manually curated GenBank accessions to 1 of the load publications, if the accession is found with the load.

begin work;

drop table if exists tmp_pre_ncbi_gene_delete;

-- Loaded from toDelete.unl
create temporary table ncbi_gene_delete (
  delete_dblink_zdb_id    text not null
);
create temp table pre_ncbi_gene_delete (
    delete_dblink_zdb_id    text not null,
    dblink_acc_num          varchar(255)
);

create index t_id_index on ncbi_gene_delete (delete_dblink_zdb_id);

\copy pre_ncbi_gene_delete from 'toDelete.unl' (delimiter '|');
insert into ncbi_gene_delete (delete_dblink_zdb_id)
 select distinct delete_dblink_zdb_id
   from pre_ncbi_gene_delete;

-- Loaded from toPreserve.unl
create temp table ncbi_dblink_to_preserve_preload (
  prsv_dblink_zdb_id    text not null,
    -- columns included in toPreserve.unl for debugging, but we ignore them for the load:
    col1 text, col2 text, col3 text, col4 text, col5 text
);
\copy ncbi_dblink_to_preserve_preload from 'toPreserve.unl' (delimiter '|');

create temp table ncbi_dblink_to_preserve as select distinct prsv_dblink_zdb_id from ncbi_dblink_to_preserve_preload;
create index prsv_id_index on ncbi_dblink_to_preserve (prsv_dblink_zdb_id);

-- Loaded from toLoad.unl
create temporary table ncbi_gene_load (
  mapped_zdb_gene_id    text not null,
  ncbi_accession        varchar(50),
  zdb_id                text,
  sequence_length        text,      
  fdbcont_zdb_id        text not null,
  load_pub_zdb_id       text not null
);

\copy ncbi_gene_load from 'toLoad.unl' (delimiter '|');

update ncbi_gene_load 
 set sequence_length = null
 where sequence_length = '';

alter table ncbi_gene_load 
  alter column sequence_length type integer USING sequence_length::integer;

update ncbi_gene_load set zdb_id = get_id_and_insert_active_data('DBLINK');

-- If there are any duplicates in the load file but with different load_pub_zdb_id, we want to keep the one with the highest priority load pub
-- Priority order is: ZDB-PUB-020723-3 > ZDB-PUB-230516-87
create temporary table ncbi_gene_load_dedup as
 select distinct on (mapped_zdb_gene_id, ncbi_accession) *
   from ncbi_gene_load
  order by mapped_zdb_gene_id, ncbi_accession,
           case load_pub_zdb_id
             when 'ZDB-PUB-020723-3' then 1
             when 'ZDB-PUB-230516-87' then 2
             else 4
           end;
drop table ncbi_gene_load;
alter table ncbi_gene_load_dedup rename to ncbi_gene_load;


--!echo 'CHECK: how many RefSeq and GenBank accessions missing length before the load'

select count(dblink_zdb_id) as noLengthBefore
  from db_link
 where dblink_length is null
   and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-38','ZDB-FDBCONT-040412-39','ZDB-FDBCONT-040527-1','ZDB-FDBCONT-040412-37','ZDB-FDBCONT-040412-42','ZDB-FDBCONT-040412-36')
   and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');

-- ▄▖▄▖▄▖▄▖▄▖▄▖▖▖▄▖  ▄ ▄▖▖ ▄▖▄▖▄▖▄▖▖ ▖▄▖
-- ▙▌▙▘▙▖▚ ▙▖▙▘▌▌▙▖  ▌▌▙▖▌ ▙▖▐ ▐ ▌▌▛▖▌▚
-- ▌ ▌▌▙▖▄▌▙▖▌▌▚▘▙▖  ▙▘▙▖▙▖▙▖▐ ▟▖▙▌▌▝▌▄▌

-- In general, we are deleting everything in toDelete.unl (ncbi_gene_delete table) and loading everything in toLoad.unl (ncbi_gene_load table).
-- However, we do want to preserve some of the accessions in toDelete.unl.
--
-- For example, if a gene has an ncbi gene id that is "not in current..." AND the ncbi gene id is in toDelete.unl,
-- then we may want to keep that gene id -- with the following conditions: if it is getting deleted, but also getting
-- replaced by something in toLoad.unl, then we should just delete it.
--
-- And if there are any ncbi gene ids that are getting deleted due to an N-to-N matching issue, those should just be
-- deleted too (even if they are in the "not in current..." list).


-- Load from notInCurrentReleaseGeneIDs.unl - contains NCBI Gene IDs that are not in current annotation release
\echo 'Loading NCBI Gene IDs that are not in current annotation release';
CREATE TEMP TABLE not_in_current_release (
     ncbi_gene_id    varchar(50) NOT NULL
);
\copy not_in_current_release from 'notInCurrentReleaseGeneIDs.unl';

\echo 'Load N-to-All conflicts from file (N-to-One, One-to-N and N-to-N)';
CREATE TEMP TABLE n_to_all_conflicts (
     gene_id    text NOT NULL,
     ncbi_id    varchar(50) NOT NULL
);
\copy n_to_all_conflicts from 'reportNtoAll.unl' (delimiter '|');


-- Preserve NCBI gene IDs that are:
-- 1. Marked for deletion
-- 2. Not in current release
-- 3. NOT being replaced by a new load
-- 4. NOT involved in N-to-N matching conflicts

CREATE TEMP TABLE ncbi_gene_delete_to_preserve AS
SELECT
    dblink_linked_recid as gene_zdb_id,
    dblink_acc_num as ncbi_gene_id,
    dblink_zdb_id
FROM db_link
         JOIN not_in_current_release
              ON dblink_acc_num = ncbi_gene_id
                  AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
WHERE dblink_zdb_id IN (
    SELECT delete_dblink_zdb_id
    FROM ncbi_gene_delete
)
-- Exclude genes being replaced
  AND (dblink_linked_recid, 'ZDB-FDBCONT-040412-1') NOT IN (
    SELECT mapped_zdb_gene_id, fdbcont_zdb_id
    FROM ncbi_gene_load
)
-- Exclude genes with N-to-N conflicts
  AND dblink_linked_recid NOT IN (
    SELECT gene_id
    FROM n_to_all_conflicts
);

DELETE FROM ncbi_gene_delete
 WHERE delete_dblink_zdb_id IN (
    SELECT dblink_zdb_id
    FROM ncbi_gene_delete_to_preserve
);

-- ▄▖▖ ▖▄   ▄▖▄▖▄▖▄▖▄▖▄▖▖▖▄▖
-- ▙▖▛▖▌▌▌  ▙▌▙▘▙▖▚ ▙▖▙▘▌▌▙▖
-- ▙▖▌▝▌▙▘  ▌ ▌▌▙▖▄▌▙▖▌▌▚▘▙▖


\echo 'Deleting from reference_protein';
\copy (select * from reference_protein where rp_dblink_zdb_id in (select * from ncbi_gene_delete)) to 'referenceProteinDeletes.unl' (delimiter '|');
delete from reference_protein where rp_dblink_zdb_id in (select * from ncbi_gene_delete);
--!echo 'Delete from zdb_active_data table and cause delete cascades on db_link records'
delete from zdb_active_data where zactvd_zdb_id in (select * from ncbi_gene_delete);

--!echo 'Delete from record_attribution table for those manually curated records but attributed to load publication'

delete from record_attribution
 where recattrib_source_zdb_id in ('ZDB-PUB-020723-3')
   and exists (select 'x' from db_link
                where recattrib_data_zdb_id = dblink_zdb_id 
                  and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
                  and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-37','ZDB-FDBCONT-040412-42','ZDB-FDBCONT-040412-36')
              )
   and not exists (select 'x' from ncbi_dblink_to_preserve where prsv_dblink_zdb_id = recattrib_data_zdb_id);

--
-- ▄▖▄▖▖ ▖▄▖▖ ▄▖▄▖▄▖▄▖
-- ▌ ▌▌▛▖▌▙▖▌ ▐ ▌ ▐ ▚
-- ▙▖▙▌▌▝▌▌ ▙▖▟▖▙▖▐ ▄▌
--
-- BEGIN CONFLICT RESOLUTION FOR NCBI GENE IDS
-- We need to remove anything that exists in the ncbi_gene_load table (accessions to load) if they are:
-- 1. NCBI Gene IDs
-- 2. Conflict with existing NCBI Gene IDs in the DB
--    a. same gene, different ncbi id
--    b. different gene, same ncbi id
-- 3. These conflicts must be such that the existing db_link is under a manually curated pub (not a load pub)

-- To resolve these conflicts, we will delete the records from ncbi_gene_load table (the load list) . They eventually get
-- dumped out for review in the many_to_many_conflicts table (logged to 'post_run_n_to_1_zdb_to_ncbi.csv')
-- They will also end up in the ncbi_report.html report for review.

-- NCBI Gene IDs that are in the load list, but already exist in db_link table for the same gene under a manually curated pub (or vice-versa)
\echo 'Deleting from ncbi_gene_load for those NCBI GeneID records that are in the load list but already exist in db_link table for the same gene (with different ncbi id) under a manually curated pub';

-- This temp table gathers the conflicts from both sides of the conflict
-- The columns will be:
--  mapped_zdb_gene_id,ncbi_accession,zdb_id,sequence_length,fdbcont_zdb_id,load_pub_zdb_id,    (from ncbi_gene_load)
--  dblink_linked_recid,dblink_acc_num,dblink_info,dblink_zdb_id,dblink_acc_num_display,dblink_length,dblink_fdbcont_zdb_id    (from db_link)
CREATE TEMP TABLE tmp_conflicts AS
SELECT
    *
FROM
    ncbi_gene_load
    INNER JOIN db_link ON
        ((mapped_zdb_gene_id = dblink_linked_recid AND ncbi_accession <> dblink_acc_num) -- same gene, different ncbi id
            OR
         (mapped_zdb_gene_id <> dblink_linked_recid AND ncbi_accession = dblink_acc_num)) -- different gene, same ncbi id
        AND fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
        AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
WHERE
    EXISTS (
        SELECT 'x' FROM record_attribution
        WHERE recattrib_data_zdb_id = dblink_zdb_id
            AND recattrib_source_zdb_id NOT IN ('ZDB-PUB-020723-3', 'ZDB-PUB-230516-87'));

-- Combine conflicts into a single table for reporting
-- This table will be dumped out to 'post_run_n_to_1_zdb_to_ncbi.csv' for review
-- It is essentially a union of the conflicts from both sides of the above tmp_conflicts table.
-- In other words, it adds the conflicting records from the ncbi_gene_load table and the db_link table as separate rows
-- The columns will be: gene_id,ncbi_id,dblink_zdb_id,load_pub,existing
CREATE temp TABLE many_to_many_conflicts AS
SELECT mapped_zdb_gene_id as gene_id, ncbi_accession as ncbi_id, zdb_id as dblink_zdb_id, load_pub_zdb_id as load_pub, FALSE as existing, 'to load' as source FROM tmp_conflicts
UNION
SELECT dblink_linked_recid, dblink_acc_num, dblink_zdb_id, '' as pub, TRUE as existing, 'manual curation' as source FROM tmp_conflicts;

-- Now delete the records from the load list and zdb_active_data table if they conflict
DELETE FROM ncbi_gene_load WHERE fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
    AND (
        (mapped_zdb_gene_id IN (SELECT gene_id FROM many_to_many_conflicts)) OR
        (ncbi_accession IN (SELECT ncbi_id FROM many_to_many_conflicts))
    );

-- We don't want to delete the manually curated records from db_link table, just the load records (I think?)
-- DELETE FROM zdb_active_data WHERE
--     zactvd_zdb_id IN (SELECT dblink_zdb_id FROM many_to_many_conflicts);

-- END CONFLICT RESOLUTION FOR NCBI GENE IDS
--
-- ▄▖▖ ▖▄   ▄▖▄▖▖ ▖▄▖▖ ▄▖▄▖▄▖▄▖
-- ▙▖▛▖▌▌▌  ▌ ▌▌▛▖▌▙▖▌ ▐ ▌ ▐ ▚
-- ▙▖▌▝▌▙▘  ▙▖▙▌▌▝▌▌ ▙▖▟▖▙▖▐ ▄▌
--


\echo 'Skipping duplicate entries in db_link table for the new records that would violate key:';
\echo 'Insert the new records into db_link table';
insert into db_link (dblink_linked_recid, dblink_acc_num, dblink_acc_num_display, dblink_info, dblink_zdb_id, dblink_length, dblink_fdbcont_zdb_id) 
select mapped_zdb_gene_id, ncbi_accession, ncbi_accession, 'uncurated: NCBI gene load ' || now(), zdb_id, sequence_length, fdbcont_zdb_id 
  from ncbi_gene_load
    ON CONFLICT (dblink_linked_recid, dblink_acc_num, dblink_fdbcont_zdb_id) DO NOTHING;

--! echo "Attribute the new db_link records to one of the 2 load publications, depending on what kind of mapping"

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select zdb_id, load_pub_zdb_id 
  from ncbi_gene_load;

--! echo "Dump all the GenPept accession associated with genes at ZFIN that are still attributed to a non-load pub"

--unload to "reportNonLoadPubGenPept"
create view reportNonLoadPubGenPept as
select recattrib_source_zdb_id, dblink_acc_num, dblink_linked_recid 
  from db_link, record_attribution 
 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-42' 
   and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
   and dblink_zdb_id = recattrib_data_zdb_id 
   and recattrib_source_zdb_id not in ('ZDB-PUB-020723-3')
group by recattrib_source_zdb_id, dblink_acc_num, dblink_linked_recid 
order by recattrib_source_zdb_id, dblink_acc_num, dblink_linked_recid;
\copy (select * from reportNonLoadPubGenPept) to 'reportNonLoadPubGenPept' with delimiter as '	' null as '';
drop view reportNonLoadPubGenPept;

--!echo 'CHECK: how many RefSeq and GenBank accessions missing length after the load'

select count(dblink_zdb_id) as noLengthAfter
  from db_link
 where dblink_length is null
   and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-38','ZDB-FDBCONT-040412-39','ZDB-FDBCONT-040527-1','ZDB-FDBCONT-040412-37','ZDB-FDBCONT-040412-42','ZDB-FDBCONT-040412-36')
   and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');

--!echo 'CHECK: how many loaded GenBank accessions missing length after the load'

select count(dblink_zdb_id) as noLenLoadedGenBank
  from db_link
 where dblink_length is null
   and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-37','ZDB-FDBCONT-040412-42','ZDB-FDBCONT-040412-36')
   and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
   and exists(select 'x' from record_attribution
               where recattrib_data_zdb_id = dblink_zdb_id
                 and recattrib_source_zdb_id in ('ZDB-PUB-020723-3','ZDB-PUB-020723-3'));


-- ▄ ▄▖▄▖▄▖▖ ▖  ▖  ▖▄▖▖ ▖▖▖  ▄▖▄▖  ▖  ▖▄▖▖ ▖▖▖
-- ▙▘▙▖▌ ▐ ▛▖▌  ▛▖▞▌▌▌▛▖▌▌▌▄▖▐ ▌▌▄▖▛▖▞▌▌▌▛▖▌▌▌
-- ▙▘▙▖▙▌▟▖▌▝▌  ▌▝ ▌▛▌▌▝▌▐   ▐ ▙▌  ▌▝ ▌▛▌▌▝▌▐

-- Many to Many report (combination of "many genes to one ncbi id" OR "one gene to many ncbi ids")
-- These are records that somehow made it through the load process and need to be cleaned up
-- We will delete these records from db_link and zdb_active_data, and dump them out for review
-- We should find out why these records were not caught before the load

-- First create a view to gather the many-to-many issues
CREATE OR REPLACE VIEW ncbi_many as
SELECT
    unnest(array_agg(dblink_linked_recid)) AS zdb_id,
    dblink_acc_num AS ncbi_id,
    dblink_acc_num AS group_id,
    'many genes to one ncbi id' as reason
FROM    db_link
WHERE    dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
GROUP BY    dblink_acc_num
HAVING    count(dblink_linked_recid) > 1
UNION
SELECT
    dblink_linked_recid AS zdb_id,
    unnest(array_agg(dblink_acc_num)) AS ncbi_id,
    dblink_linked_recid AS group_id,
    'one gene to many ncbi ids' as reason
FROM        db_link
WHERE        dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
GROUP BY        dblink_linked_recid
HAVING        count(dblink_acc_num) > 1;

-- Now delete the recent NCBI Gene IDs (from 8/29/25) for each ZDB gene record where there are issues
-- Get these by joining to db_link table
CREATE OR REPLACE VIEW ncbi_many_full as
SELECT marker.mrkr_abbrev as symbol, nid.zdb_id, nid.ncbi_id, nid.group_id, nid.reason, db_link.dblink_info, db_link.dblink_zdb_id, db_link.dblink_fdbcont_zdb_id, string_agg(record_attribution.recattrib_source_zdb_id, ',') AS source_pubs
FROM
    ncbi_many nid
    LEFT JOIN db_link ON zdb_id = dblink_linked_recid
        AND ncbi_id = dblink_acc_num
        AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
    LEFT JOIN marker ON mrkr_zdb_id = dblink_linked_recid
    LEFT JOIN record_attribution ON recattrib_data_zdb_id = dblink_zdb_id
GROUP BY marker.mrkr_abbrev, nid.zdb_id, nid.ncbi_id, nid.group_id, nid.reason, db_link.dblink_info, db_link.dblink_zdb_id, db_link.dblink_fdbcont_zdb_id;

\copy (select * from ncbi_many_full order by group_id, dblink_zdb_id) to 'existing_many_to_many_report.csv' with header csv;

INSERT INTO many_to_many_conflicts (
    SELECT
        string_agg(DISTINCT zdb_id, ' ') AS gene_id,
        string_agg(DISTINCT ncbi_id, ' ') AS ncbi_id,
        string_agg(DISTINCT dblink_zdb_id, ' ') AS dblink_zdb_id,
        string_agg(DISTINCT source_pubs, ' ') AS source_pubs,
        FALSE AS existing,
        'post load cleanup' AS source
    FROM
        ncbi_many_full
    GROUP BY
        group_id
    ORDER BY
        group_id);

delete from zdb_active_data where zactvd_zdb_id in (select dblink_zdb_id from ncbi_many_full);

-- Many to Many report should be empty, but if not, dump it out for review. Also, delete those records from db_link and zdb_active_data
\copy (SELECT DISTINCT * FROM many_to_many_conflicts ORDER BY gene_id, ncbi_id) TO 'post_run_n_to_1_zdb_to_ncbi.csv' WITH HEADER CSV;
-- ▄▖▖ ▖▄   ▖  ▖▄▖▖ ▖▖▖  ▄▖▄▖  ▖  ▖▄▖▖ ▖▖▖
-- ▙▖▛▖▌▌▌  ▛▖▞▌▌▌▛▖▌▌▌▄▖▐ ▌▌▄▖▛▖▞▌▌▌▛▖▌▌▌
-- ▙▖▌▝▌▙▘  ▌▝ ▌▛▌▌▝▌▐   ▐ ▙▌  ▌▝ ▌▛▌▌▝▌▐



-- ▄ ▄▖▄▖▄▖▖ ▖  ▄▖▖ ▖▖ ▖▄▖▄▖▄▖▄▖▄▖▄▖▖ ▖  ▄▖▄▖▄▖▄▖▖▖▄▖
-- ▙▘▙▖▌ ▐ ▛▖▌  ▌▌▛▖▌▛▖▌▌▌▐ ▌▌▐ ▐ ▌▌▛▖▌  ▚ ▐ ▌▌▐ ▌▌▚
-- ▙▘▙▖▙▌▟▖▌▝▌  ▛▌▌▝▌▌▝▌▙▌▐ ▛▌▐ ▟▖▙▌▌▝▌  ▄▌▐ ▛▌▐ ▙▌▄▌
--
-- Logic to handle marker_annotation_status
-- It is safe to say any gene that has an NCBI Gene ID matched by the rna matching method should be considered "Current"
-- with the exception of those genes that are explicitly marked as "Not in current annotation release set"
--
-- Any other gene with an NCBI Gene ID that is matched by the other matching methods should be considered "Unknown".
-- The RNA matching method uses the publication ZDB-PUB-020723-3
--
-- ie:
-- Start with the zdb gene:
--
-- 1. Does the zdb gene have an NCBI Gene ID?
--    - No: Classify as "Unknown"
--    - Yes: Proceed to step 2
--
-- 2. Is the associated NCBI Gene ID for zdb gene explicitly marked as "Not in current annotation release set"?
--     - Yes: Classify as "Not in current annotation release set"
--     - No: Proceed to step 3
--
-- 3. Is the associated NCBI Gene ID attributed with the rna matching method (ZDB-PUB-020723-3)?
--     - Yes: Classify as "Current"
--     - No: Classify as "Unknown"
--
-- End

\echo 'Clearing existing marker annotation status entries';

-- Clear all existing marker_annotation_status entries since we're recalculating the entire state
DELETE FROM marker_annotation_status;

\echo 'Calculating and inserting marker annotation status based on NCBI Gene ID matches';

-- Insert marker_annotation_status entries based on the logic:
-- 1. If gene has NCBI Gene ID in notInCurrentReleaseGeneIDs.unl -> "Not in current annotation release" (13)
-- 2. Else if gene has NCBI Gene ID attributed with one of the three load pubs -> "Current" (12)
-- 3. Else (other matching methods or no NCBI Gene ID) -> "Unknown" (no entry)
INSERT INTO marker_annotation_status (mas_mrkr_zdb_id, mas_vt_pk_id)
WITH eligible_records AS (
    SELECT DISTINCT
        dl.dblink_linked_recid,
        CASE
            -- Step 2: If NCBI Gene ID is in notInCurrentReleaseGeneIDs.unl, mark as "Not in current annotation release"
            WHEN EXISTS (
                SELECT 1 FROM not_in_current_release nic
                WHERE nic.ncbi_gene_id = dl.dblink_acc_num
            ) THEN 13  -- "Not in current annotation release"
        -- Step 3: If has NCBI Gene ID attributed with load pub, mark as "Current"
            WHEN EXISTS (
                SELECT 1 FROM record_attribution ra
                WHERE ra.recattrib_data_zdb_id = dl.dblink_zdb_id
                  AND ra.recattrib_source_zdb_id IN ('ZDB-PUB-020723-3', 'ZDB-PUB-230516-87')
            ) THEN 12  -- "Current"
            END AS annotation_status
    FROM db_link dl
    WHERE dl.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'  -- NCBI Gene ID
)
SELECT dblink_linked_recid, annotation_status
FROM eligible_records
WHERE annotation_status IS NOT NULL;

DELETE FROM marker_assembly
WHERE ma_a_pk_id = (SELECT a_pk_id FROM assembly WHERE a_name = 'GRCz12tu')
AND ma_mrkr_zdb_id IN ( SELECT dblink_linked_recid FROM ncbi_gene_delete JOIN db_link
            ON delete_dblink_zdb_id = dblink_zdb_id AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1');

\echo 'Marker annotation status update complete';
-- ▄▖▖ ▖▄   ▄▖▖ ▖▖ ▖▄▖▄▖▄▖▄▖▄▖▄▖▖ ▖  ▄▖▄▖▄▖▄▖▖▖▄▖
-- ▙▖▛▖▌▌▌  ▌▌▛▖▌▛▖▌▌▌▐ ▌▌▐ ▐ ▌▌▛▖▌  ▚ ▐ ▌▌▐ ▌▌▚
-- ▙▖▌▝▌▙▘  ▛▌▌▝▌▌▝▌▙▌▐ ▛▌▐ ▟▖▙▌▌▝▌  ▄▌▐ ▛▌▐ ▙▌▄▌
--


commit work;


