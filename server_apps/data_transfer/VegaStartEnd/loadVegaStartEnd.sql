begin work ;

create temp table tmp_vega (chrom varchar(20), 
       	    	  	   	  mrkrType varchar(10),
				  start int,
				  end int,
				  character1 varchar(2),
				  strand varchar(2),	
				  character2 varchar(2),
				  ottdarg varchar(50))
with no log;

load from process_zebrafish_VEGA55_55.gff3
 insert into tmp_vega;

create index tmp_vega_ottdarg_index
  on tmp_vega(ottdarg)
 using btree in idxdbs1;

delete from sequence_feature_chromosome_location
 where sfcl_location_source = 'VegaStartEndLoader';

insert into sequence_feature_chromosome_location (sfcl_data_Zdb_id, 
       	    			       sfcl_chromosome,
				       sfcl_start,
				       sfcl_end,
				       sfcl_acc_num,
				       sfcl_location_source,
				       sfcl_location_Subsource,
				       sfcl_fdb_db_id)
select distinct dblink_linked_recid,
       		chrom,
		start,
		end,
		ottdarg,
		'VegaStartEndLoader',
		'directAnnotationToTscript',
		fdb_db_pk_id
  from db_link, tmp_vega, foreign_db, foreign_db_contains
  where dblink_Fdbcont_zdb_id = fdbcont_Zdb_id
  and dblink_acc_num = ottdarg
  and fdb_db_pk_id = fdbcont_fdb_db_id
 and ottdarg like 'OTTDARG%';

insert into sequence_feature_chromosome_location (sfcl_data_Zdb_id, 
       	    			       sfcl_chromosome,
				       sfcl_start,
				       sfcl_end,
				       sfcl_acc_num,
				       sfcl_location_source,
				       sfcl_location_Subsource,
				       sfcl_fdb_db_id)
select distinct mrel_mrkr_1_zdb_id,
       		chrom,
		start,
		end,
		ottdarg,
		'VegaStartEndLoader',
		'pullThroughToGene',
		fdb_db_pk_id
  from db_link, tmp_vega, foreign_db, foreign_db_contains, marker_relationship
  where dblink_Fdbcont_zdb_id = fdbcont_Zdb_id
  and dblink_acc_num = ottdarg
  and fdb_db_pk_id = fdbcont_fdb_db_id
 and ottdarg like 'OTTDARG%'
 and mrel_mrkr_2_zdb_id = dblink_linked_recid
 and mrel_mrkr_1_zdb_id like 'ZDB-GENE%'
 and mrel_mrkr_2_zdb_id like 'ZDB-TSCRIPT%'
;

delete from sequence_feature_chromosome_location
 where sfcl_chromosome in ('AB','U','0')
 and sfcl_location_source = 'VegaStartEndLoader';
				  
commit work;

--rollback work ;