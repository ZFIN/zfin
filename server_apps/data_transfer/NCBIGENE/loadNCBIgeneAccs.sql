-- loadNCBIgeneAccs.sql
--
-- This SQL script deletes the db_link records according to the delete list, toDelete.unl.
-- And it loads all the following kinds of db_link records according to the add list, toLoad.unl.
-- 1) NCBI Gene Ids
-- 2) RefSeq accessioons (including RefSeq RNA, RefPept, RefSeq DNA)
-- 3) GenBank accessions (including GenBank RNA, GenPept, GenBank DNA)
-- The script also attribute the manually curated GenBank accessions to 1 of the load publications, if the accession is found with the load.

begin work;

-- Loaded from toDelete.unl
create temporary table ncbi_gene_delete (
  delete_dblink_zdb_id    text not null
);

create index t_id_index on ncbi_gene_delete (delete_dblink_zdb_id);

\copy ncbi_gene_delete from 'toDelete.unl';

-- Loaded from toPreserve.unl
create temporary table ncbi_dblink_to_preserve (
  prsv_dblink_zdb_id    text not null
);
create index prsv_id_index on ncbi_dblink_to_preserve (prsv_dblink_zdb_id);
\copy ncbi_dblink_to_preserve from 'toPreserve.unl';

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

--!echo 'CHECK: how many RefSeq and GenBank accessions missing length before the load'

select count(dblink_zdb_id) as noLengthBefore
  from db_link
 where dblink_length is null
   and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-38','ZDB-FDBCONT-040412-39','ZDB-FDBCONT-040527-1','ZDB-FDBCONT-040412-37','ZDB-FDBCONT-040412-42','ZDB-FDBCONT-040412-36')
   and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%');

--!echo 'Delete from zdb_active_data table and cause delete cascades on db_link records'

-- BEGIN LOGIC FOR NOT_IN_CURRENT_ANNOTATION_RELEASE {
    -- Delete from marker_annotation_status for (NCBI GeneID) dblinks. Must use join to get the mrkr_id from the db_link table.
    create temporary table dblink_to_ncbi_delete as (
    select delete_dblink_zdb_id as dblink_id, dblink_linked_recid as mrkr_id, dblink_acc_num as ncbi_id
        from ncbi_gene_delete
        join db_link on delete_dblink_zdb_id = dblink_zdb_id and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1' );

    delete from marker_annotation_status where mas_mrkr_zdb_id in (select mrkr_id from dblink_to_ncbi_delete);

    -- Add the "Current" flag to the db_link records that were just loaded (12 is the id for "Current" in vocabulary_term)
    -- First remove any existing flags for the same records
    delete from marker_annotation_status
    where mas_mrkr_zdb_id in (select mapped_zdb_gene_id from ncbi_gene_load where fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1');
    insert into marker_annotation_status (mas_mrkr_zdb_id, mas_vt_pk_id)
    select mapped_zdb_gene_id, 12 as vocab_term_id from ncbi_gene_load where fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1';

    -- Handle "not in current annotation release" db_link records
    create temporary table ncbi_gene_not_in_current(
      ncbi_gene_id    text not null
    );
    create temporary table ncbi_zdb_gene_not_in_current(
      ncbi_gene_id    text not null,
      mrkr_zdb_id    text not null,
      UNIQUE(ncbi_gene_id, mrkr_zdb_id)
    );
    -- This will create an empty file if it doesn't exist
    \! touch notInCurrentReleaseGeneIDs.unl
    \copy ncbi_gene_not_in_current  from 'notInCurrentReleaseGeneIDs.unl';

    -- map the NCBI Gene IDs to ZFIN marker IDs
    insert into ncbi_zdb_gene_not_in_current (ncbi_gene_id, mrkr_zdb_id)
    select distinct ncbi_gene_id, mrkr_id
    from dblink_to_ncbi_delete join ncbi_gene_not_in_current
                      on ncbi_id = ncbi_gene_id;

    -- Add extra mappings for genes that were not marked as "to be deleted" but are in the notInCurrentReleaseGeneIDs.unl file
    INSERT INTO ncbi_zdb_gene_not_in_current
        SELECT DISTINCT ncbi_gene_id, dblink_linked_recid
        FROM ncbi_gene_not_in_current JOIN db_link ON dblink_acc_num = ncbi_gene_id AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
        ON CONFLICT DO NOTHING;

    -- 13 is the id for "Not in current annotation release" in vocabulary_term
    delete from marker_annotation_status
    where mas_mrkr_zdb_id in (select mrkr_zdb_id from ncbi_zdb_gene_not_in_current);

-- if the gene_id in ncbi_zdb_gene_not_in_current is getting loaded in this batch (likely due to a history of multiple NCBI Gene IDs for one gene)
-- then we want to preserve the "Current" status instead of changing it to "Not in current annotation release"
    insert into marker_annotation_status (mas_mrkr_zdb_id, mas_vt_pk_id)
    select mrkr_zdb_id, 13 as vocab_term_id from ncbi_zdb_gene_not_in_current
        WHERE mrkr_zdb_id NOT IN (SELECT mapped_zdb_gene_id FROM ncbi_gene_load WHERE fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1')
        on conflict (mas_mrkr_zdb_id) do update set mas_vt_pk_id = 13;

    -- Anything in the to_delete table that matches the 13 vocab_term_id should be preserved
    -- This query removes them from the ncbi_gene_delete table, thus preserving them
    -- But first, if there are new records coming in for the same gene (zdb_id), we want to keep those instead of the old ones
    -- So we create a temp table of those deletes that should stay deletes (deletes_to_keep temp table)
    SELECT dblink_zdb_id
    into temp table deletes_to_keep
    from ncbi_gene_load
        inner join db_link on
        mapped_zdb_gene_id = dblink_linked_recid
        and fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
        and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1' ;

    DELETE FROM ncbi_gene_delete
    WHERE EXISTS (
        SELECT 1
        FROM db_link
        WHERE delete_dblink_zdb_id = dblink_zdb_id
          AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
          AND (dblink_linked_recid, dblink_acc_num) IN (
            SELECT mrkr_zdb_id, ncbi_gene_id
            FROM ncbi_zdb_gene_not_in_current
        )
    )
    AND delete_dblink_zdb_id NOT IN (SELECT * FROM deletes_to_keep);

    -- Question: In testing this only preserves about 50 records. Previously we were preserving about 2890 records.
    -- I think we want a report of all "not_in_current" genes

-- } END LOGIC FOR NOT_IN_CURRENT_ANNOTATION_RELEASE

\echo 'Deleting from reference_protein';
\copy (select * from reference_protein where rp_dblink_zdb_id in (select * from ncbi_gene_delete)) to 'referenceProteinDeletes.unl' (delimiter '|');
delete from reference_protein where rp_dblink_zdb_id in (select * from ncbi_gene_delete);
delete from zdb_active_data where zactvd_zdb_id in (select * from ncbi_gene_delete);

--!echo 'Delete from record_attribution table for those manually curated records but attributed to load publication'

delete from record_attribution
 where recattrib_source_zdb_id in ('ZDB-PUB-020723-3','ZDB-PUB-130725-2')
   and exists (select 'x' from db_link
                where recattrib_data_zdb_id = dblink_zdb_id 
                  and (dblink_linked_recid like 'ZDB-GENE%' or dblink_linked_recid like '%RNAG%')
                  and dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-37','ZDB-FDBCONT-040412-42','ZDB-FDBCONT-040412-36')
              )
   and not exists (select 'x' from ncbi_dblink_to_preserve where prsv_dblink_zdb_id = recattrib_data_zdb_id);

-- NCBI Gene IDs that are in the load list, but already exist in db_link table for the same gene under a manually curated pub
\echo 'Deleting from ncbi_gene_load for those NCBI GeneID records that are in the load list but already exist in db_link table for the same gene (with different ncbi id) under a manually curated pub';
select * into temp table manual_conflict_warnings from ncbi_gene_load
         inner join db_link on mapped_zdb_gene_id = dblink_linked_recid
                                   and ncbi_accession <> dblink_acc_num
                                   and fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
                                   and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
 where TRUE
   and exists (select 'x' from record_attribution
                where recattrib_data_zdb_id = dblink_zdb_id
                  and recattrib_source_zdb_id not in ('ZDB-PUB-020723-3','ZDB-PUB-130725-2')
              );
-- \copy (select * from manual_conflict_warnings) to 'manual_conflict_warnings.csv' with header csv;
delete from ncbi_gene_load where fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
 and mapped_zdb_gene_id in (select mapped_zdb_gene_id from manual_conflict_warnings);

-- NCBI Gene IDs that are in the load list, but already exist in db_link table for a different gene
\echo 'Deleting from ncbi_gene_load for those NCBI GeneID records that are in the load list but already exist in db_link table for a different gene';
select mapped_zdb_gene_id, ncbi_accession, zdb_id, load_pub_zdb_id, dblink_linked_recid as existing_zdb_id,
        dblink_zdb_id as existing_dblink_id, dblink_info, recattrib_source_zdb_id as existing_load_pub_zdb_id
       into temp table n_gene_1_ncbi_conflict_warnings  from ncbi_gene_load
         inner join db_link on ncbi_accession = dblink_acc_num
                                   and mapped_zdb_gene_id <> dblink_linked_recid
                                   and fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
                                   and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
        inner join record_attribution on recattrib_data_zdb_id = dblink_zdb_id;

-- Get the right hand side of that join too
select dblink_linked_recid as gene_id, dblink_acc_num as ncbi_id, dblink_zdb_id, recattrib_source_zdb_id as load_pub, true as existing
 into temp table post_run_n_to_1_zdb_to_ncbi from db_link
         inner join record_attribution on recattrib_data_zdb_id = dblink_zdb_id
 where dblink_acc_num in (select ncbi_accession from n_gene_1_ncbi_conflict_warnings)
   and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
   and recattrib_source_zdb_id in ('ZDB-PUB-020723-3','ZDB-PUB-130725-2', 'ZDB-PUB-230516-87');

insert into post_run_n_to_1_zdb_to_ncbi
    (SELECT mapped_zdb_gene_id as gene_id, ncbi_accession as ncbi_id, zdb_id, load_pub_zdb_id as load_pub, false as existing
       from n_gene_1_ncbi_conflict_warnings);

\copy (select * from post_run_n_to_1_zdb_to_ncbi order by ncbi_id) to 'post_run_n_to_1_zdb_to_ncbi.csv' with header csv;
delete from ncbi_gene_load where fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
 and ncbi_accession in (select ncbi_id from post_run_n_to_1_zdb_to_ncbi);

delete from zdb_active_data where zactvd_zdb_id in (select dblink_zdb_id from post_run_n_to_1_zdb_to_ncbi);

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
   and recattrib_source_zdb_id not in ('ZDB-PUB-020723-3','ZDB-PUB-130725-2') 
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

-- Mark any remaining Genes as "not in current annotation release" if they have a NCBI Gene ID that is in the notInCurrentReleaseGeneIDs.unl file
WITH matched_not_in_current  as (
    select * from ncbi_zdb_gene_not_in_current join db_link on dblink_linked_recid = mrkr_zdb_id and dblink_acc_num = ncbi_gene_id and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
)
insert into marker_annotation_status (mas_mrkr_zdb_id, mas_vt_pk_id)
 select mrkr_zdb_id, 13
 from matched_not_in_current
on conflict (mas_mrkr_zdb_id) do update
 set mas_vt_pk_id = 13;


-- Many to Many report:
SELECT
    unnest(array_agg(dblink_linked_recid)) AS zdb_id,
    dblink_acc_num AS ncbi_id,
    dblink_acc_num AS group_id,
    'many genes to one ncbi id' as reason
INTO temp TABLE ncbi_many
FROM    db_link
WHERE    dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
GROUP BY    dblink_acc_num
HAVING    count(dblink_linked_recid) > 1;

-- Now get all ZDB gene records that have more than one NCBI Gene ID
INSERT INTO ncbi_many (
    SELECT
        dblink_linked_recid AS zdb_id,
        unnest(array_agg(dblink_acc_num)) AS ncbi_id,
        dblink_linked_recid AS group_id,
        'one gene to many ncbi ids' as reason
    FROM        db_link
    WHERE        dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
    GROUP BY        dblink_linked_recid
    HAVING        count(dblink_acc_num) > 1);

-- Now delete the recent NCBI Gene IDs (from 8/29/25) for each ZDB gene record where there are issues
-- Get these by joining to db_link table
SELECT marker.mrkr_abbrev as symbol, nid.zdb_id, nid.ncbi_id, nid.group_id, nid.reason, db_link.dblink_info, db_link.dblink_zdb_id, db_link.dblink_fdbcont_zdb_id, string_agg(record_attribution.recattrib_source_zdb_id, ',') AS source_pubs
INTO temp TABLE ncbi_many_full
FROM
    ncbi_many nid
    LEFT JOIN db_link ON zdb_id = dblink_linked_recid
    AND ncbi_id = dblink_acc_num
    AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
    LEFT JOIN marker ON mrkr_zdb_id = dblink_linked_recid
    LEFT JOIN record_attribution ON recattrib_data_zdb_id = dblink_zdb_id
GROUP BY marker.mrkr_abbrev, nid.zdb_id, nid.ncbi_id, nid.group_id, nid.reason, db_link.dblink_info, db_link.dblink_zdb_id, db_link.dblink_fdbcont_zdb_id;

\copy (select * from ncbi_many_full order by group_id, dblink_zdb_id) to 'existing_many_to_many_report.csv' with header csv;
-- End of Many to Many report

commit work;


