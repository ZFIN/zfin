begin work;

create temp table ncbi_gene_delete (
  delete_dblink_zdb_id    varchar(50) not null
) with no log;

load from 'toDelete.unl' 
  insert into ncbi_gene_delete;

create temp table ncbi_gene_load (
  mapped_zdb_gene_id    varchar(50) not null,
  ncbi_accession        varchar(50) not null,
  zdb_id                varchar(50),
  seqence_length        integer,      
  fdbcont_zdb_id        varchar(50) not null,
  load_pub_zdb_id       varchar(50) not null
) with no log;

load from 'toLoad.unl' 
  insert into ncbi_gene_load;
  
update ncbi_gene_load set zdb_id = get_id("DBLINK");

!echo 'CHECK: how many RefSeq and GenBank accessions missing length before the load'
select count(dblink_zdb_id) as noLengthBefore
  from db_link
 where dblink_length is null
   and dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-38","ZDB-FDBCONT-040412-39","ZDB-FDBCONT-040527-1","ZDB-FDBCONT-040412-37","ZDB-FDBCONT-040412-42","ZDB-FDBCONT-040412-36")
   and dblink_linked_recid like "ZDB-GENE%";

!echo 'Delete from zdb_active_data and cause delete cascades on db_link records'
delete from zdb_active_data
 where exists (select 'x' from ncbi_gene_delete where zactvd_zdb_id = delete_dblink_zdb_id);

!echo 'Insert into zdb_active_data the new zdb ids of db_link records'
insert into zdb_active_data select zdb_id from ncbi_gene_load;

!echo 'Insert the new records into db_link table'
insert into db_link (dblink_linked_recid, dblink_acc_num, dblink_info, dblink_zdb_id, dblink_length, dblink_fdbcont_zdb_id) 
select mapped_zdb_gene_id, ncbi_accession, "uncurated: NCBI gene load " || TODAY, zdb_id, seqence_length, fdbcont_zdb_id 
  from ncbi_gene_load;
    
! echo "Attribute the new db_link records to one of the 2 load publications, depending on what kind of mapping"
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select zdb_id, load_pub_zdb_id 
  from ncbi_gene_load;


!echo 'CHECK: how many RefSeq and GenBank accessions missing length after the load'
select count(dblink_zdb_id) as noLengthAfter
  from db_link
 where dblink_length is null
   and dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-38","ZDB-FDBCONT-040412-39","ZDB-FDBCONT-040527-1","ZDB-FDBCONT-040412-37","ZDB-FDBCONT-040412-42","ZDB-FDBCONT-040412-36")
   and dblink_linked_recid like "ZDB-GENE%";

!echo 'CHECK: how many loaded GenBank accessions missing length after the load'
select count(dblink_zdb_id) as noLenLoadedGenBank
  from db_link
 where dblink_length is null
   and dblink_fdbcont_zdb_id in ("ZDB-FDBCONT-040412-37","ZDB-FDBCONT-040412-42","ZDB-FDBCONT-040412-36")
   and dblink_linked_recid like "ZDB-GENE%"
   and exists(select "x" from record_attribution
               where recattrib_data_zdb_id = dblink_zdb_id
                 and recattrib_source_zdb_id in ("ZDB-PUB-020723-3","ZDB-PUB-020723-3"));

--rollback work;

commit work;


