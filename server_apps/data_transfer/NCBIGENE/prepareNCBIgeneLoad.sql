-- prepareNCBIgeneLoad.sql
--
-- This SQL script prepares the following 2 unload files to be used by the code in the Perl script to do the mapping and the final loading script.
--
-- 1) toMap.unl      
-- this is a list of gene zdb ids that are supported by non-loaded GenBank RNA accessions and genes on this list will be processed to map to NCBI gene records
--
-- 2) toDelete.unl      
-- this is a list of db_link zdb ids that are to be deleted before doing the loading with a new list of db_link zdb ids


begin work;

set pdqpriority 50;
                             
!echo 'Attribute the UniGene accessions missing attribution to one of the NCBI gene load publications (ZDB-PUB-020723-3)'
!echo 'This piece of SQL for FB case 9915 should be kept because curator may accidentally curate such accessions which should be maintained by the load with no attribution again'

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select dblink_zdb_id,"ZDB-PUB-020723-3" from db_link
   where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-44"      -- UniGene
     and not exists (select "x" from record_attribution
                      where recattrib_data_zdb_id = dblink_zdb_id);
                      
create temp table pre_delete (dblink_loaded_zdb_id		varchar(50)
                             )with no log;

!echo 'Prepare the delete list with accession records in record_attribution table attributed to NCBI gene load publications (ZDB-PUB-020723-3, ZDB-PUB-130725-2)'
		
insert into pre_delete
select recattrib_data_zdb_id from record_attribution
 where recattrib_source_zdb_id in ("ZDB-PUB-020723-3", "ZDB-PUB-130725-2");

create index pd_data_id_index
 on pre_delete(dblink_loaded_zdb_id) using btree in idxdbs3;

update statistics high for table pre_delete;

create temp table db_link_in_expression_experiment2
      (dblink_ee_zdb_id		varchar(50)
      )with no log;

!echo 'Prepare a list of db_link records also in expression_experiment2 table attributed only to NCBI gene load publications (ZDB-PUB-020723-3, ZDB-PUB-130725-2)'

insert into db_link_in_expression_experiment2
select distinct xpatex_dblink_zdb_id
 from expression_experiment2
 where exists (select "x" from record_attribution
               where xpatex_dblink_zdb_id = recattrib_data_zdb_id
               and recattrib_source_zdb_id in ("ZDB-PUB-020723-3","ZDB-PUB-130725-2"))
 and not exists (select "x" from record_attribution
                 where xpatex_dblink_zdb_id = recattrib_data_zdb_id
                 and recattrib_source_zdb_id not in ("ZDB-PUB-020723-3","ZDB-PUB-130725-2"));

!echo 'Retain those db_link records that are also in expression_experiment2 table, which are only attributed to the load pub'

delete from pre_delete
 where exists (select "x" from db_link_in_expression_experiment2
                where dblink_loaded_zdb_id = dblink_ee_zdb_id);

!echo 'Analyze what kinds of data in pre_delete table'

select distinct dblink_loaded_zdb_id[1,9], count(dblink_loaded_zdb_id) from pre_delete group by dblink_loaded_zdb_id[1,9]; 

!echo 'Analyze what kinds of dblink_linked_recid in db_link table with dblink_zdb_id in pre_delete table'

select distinct dblink_linked_recid[1,8], count(dblink_linked_recid) 
  from pre_delete, db_link
 where dblink_loaded_zdb_id = dblink_zdb_id
 group by dblink_linked_recid[1,8]; 
 
!echo 'Retain those db_link accessions which are not related to gene or pseudogene.'
delete from pre_delete
 where exists (select "x" from db_link 
                where dblink_loaded_zdb_id = dblink_zdb_id
                  and dblink_linked_recid not like "ZDB-GENE%");
		  
!echo 'Retain those attributed to a publication other than NCBI gene load publication.'

delete from pre_delete
 where exists (select "x" from record_attribution 
                where recattrib_data_zdb_id = dblink_loaded_zdb_id
                  and recattrib_source_zdb_id not in ("ZDB-PUB-020723-3","ZDB-PUB-130725-2"));

!echo 'Analyze what kinds of data in pre_delete table'

select distinct dblink_loaded_zdb_id[1,9], count(dblink_loaded_zdb_id) from pre_delete group by dblink_loaded_zdb_id[1,9]; 

!echo 'Analyze what kinds of dblink_linked_recid in db_link table with dblink_zdb_id in pre_delete table'

select distinct dblink_linked_recid[1,8], count(dblink_linked_recid) 
  from pre_delete, db_link
 where dblink_loaded_zdb_id = dblink_zdb_id
 group by dblink_linked_recid[1,8]; 
 
!echo 'Retain those db_link accessions which are not related to gene or pseudogene.'
delete from pre_delete
 where exists (select "x" from db_link 
                where dblink_loaded_zdb_id = dblink_zdb_id
                  and dblink_linked_recid not like "ZDB-GENE%");

create temp table backup_accession_length (	
        temp_acc_num	       varchar(30) not null,
	temp_length            integer,
	temp_fdbcont_zdb_id    varchar(50)
	                                  )with no log;
                             
!echo 'Get the list of the genes supported by GenBank RNA sequenecs'

select dblink_linked_recid as gene
  from db_link
 where dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-37"
   and dblink_linked_recid like "ZDB-GENE%"
   and not exists(select "x" from pre_delete
                   where dblink_loaded_zdb_id = dblink_zdb_id)
   and not exists(select "x" from marker 
                   where mrkr_abbrev like "WITHDRAWN%" 
                     and dblink_linked_recid = mrkr_zdb_id)
union
select mrel_mrkr_1_zdb_id as gene
  from marker_relationship
 where mrel_type = "gene encodes small segment"
   and exists(select "x" from db_link
               where dblink_linked_recid = mrel_mrkr_2_zdb_id
                 and dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-37"
                 and not exists(select "x" from pre_delete
                                 where dblink_loaded_zdb_id = dblink_zdb_id))
  and not exists(select "x" from marker 
                  where mrkr_abbrev like "WITHDRAWN%" 
                    and mrel_mrkr_1_zdb_id = mrkr_zdb_id)
into temp tmp_genes_supported_by_rna with no log;

select count(gene) as numberOfGenesWithRNAevidence from tmp_genes_supported_by_rna;

--!echo 'Pseudo-genes involved in the load'

--select distinct gene from tmp_genes_supported_by_rna where gene not like "ZDB-GENE-%"; 

!echo 'Dump the list of genes supported by GenBank RNA sequenecs, as the start set on ZFIN end for mapping'

unload to '<!--|ROOT_PATH|-->/server_apps/data_transfer/NCBIGENE/toMap.unl' select distinct gene from tmp_genes_supported_by_rna order by gene;

!echo 'Dump the delete list'

unload to '<!--|ROOT_PATH|-->/server_apps/data_transfer/NCBIGENE/toDelete.unl' select * from pre_delete order by dblink_loaded_zdb_id;

--rollback work;

commit work;


