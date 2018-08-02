begin work ;

create temp table tmp_ncbi (species int, 
       	    	  	    chrom varchar(20),
			    start int,
			    end int,
			    chr_orient varchar(50),
			    contig varchar(50),
			    ctg_start int,
			    ctg_stop int,
			    ctg_orient varchar(50),
			    feature_name varchar(50),
			    accnum varchar(50),
			    feature_type varchar(50),
			    group_label varchar(50),
			    transcript varchar(50),
			    evidence_code varchar(50));

\copy tmp_ncbi from process_seq_gene.md ;

select first 1 * from tmp_ncbi;

create index tmp_ncbi_ottdarg_index
  on tmp_ncbi(accnum);

delete from sequence_feature_chromosome_location_generated
 where sfclg_location_source = 'NCBIStartEndLoader';

insert into sequence_feature_chromosome_location_generated (sfclg_data_Zdb_id, 
       	    			       sfclg_chromosome,
				       sfclg_start,
				       sfclg_end,
				       sfclg_acc_num,
				       sfclg_location_source,
				       sfclg_fdb_db_id)
select distinct dblink_linked_recid,
       		chrom,
		start,
		end,
		accnum,
		'NCBIStartEndLoader',
		fdb_db_pk_id
  from db_link, tmp_ncbi, foreign_db, foreign_db_contains
  where dblink_Fdbcont_zdb_id = fdbcont_Zdb_id
  and dblink_acc_num = accnum
  and fdb_db_pk_id = fdbcont_fdb_db_id
  and feature_type = 'GENE'
  and fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1';



delete from sequence_feature_chromosome_location_generated
 where sfclg_chromosome in ('AB','U','0')
 and sfclg_location_source = 'NCBIStartEndLoader';
				  
commit work;

--rollback work ;
