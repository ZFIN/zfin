-- loadNCBIgeneAccs.sql
--
-- This SQL script deletes the db_link records according to the delete list, toDelete.unl.
-- And it loads all the following kinds of db_link records according to the add list, toLoad.unl.
-- 1) NCBI Gene Ids
-- 2) RefSeq accessioons (including RefSeq RNA, RefPept, RefSeq DNA)
-- 3) GenBank accessions (including GenBank RNA, GenPept, GenBank DNA)
-- The script also attribute the manually curated GenBank accessions to 1 of the load publications, if the accession is found with the load.

begin work;

create temporary table ncbi_gene_delete (
  delete_dblink_zdb_id    text not null
);

create index t_id_index on ncbi_gene_delete (delete_dblink_zdb_id);

\copy ncbi_gene_delete from 'toDelete.unl';

create temporary table ncbi_dblink_to_preserve (
  prsv_dblink_zdb_id    text not null
);
create index prsv_id_index on ncbi_dblink_to_preserve (prsv_dblink_zdb_id);
\copy ncbi_dblink_to_preserve from 'toPreserve.unl';

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
  mrkr_zdb_id    text not null
);
-- This will create an empty file if it doesn't exist
\! touch notInCurrentReleaseGeneIDs.unl
\copy ncbi_gene_not_in_current  from 'notInCurrentReleaseGeneIDs.unl';

-- map the NCBI Gene IDs to ZFIN marker IDs
insert into ncbi_zdb_gene_not_in_current (ncbi_gene_id, mrkr_zdb_id)
select ncbi_gene_id, mrkr_id
from dblink_to_ncbi_delete join ncbi_gene_not_in_current
                  on ncbi_id = ncbi_gene_id;

-- 13 is the id for "Not in current annotation release" in vocabulary_term
delete from marker_annotation_status
where mas_mrkr_zdb_id in (select mrkr_zdb_id from ncbi_zdb_gene_not_in_current);
insert into marker_annotation_status (mas_mrkr_zdb_id, mas_vt_pk_id)
select mrkr_zdb_id, 13 as vocab_term_id from ncbi_zdb_gene_not_in_current;


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

--rollback work;

commit work;


