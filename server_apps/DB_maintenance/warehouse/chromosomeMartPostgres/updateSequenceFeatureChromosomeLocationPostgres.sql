begin work;

delete from sequence_feature_chromosome_location_generated
 where sfclg_location_source = 'ZfinGbrowseStartEndLoader';

delete from sequence_feature_chromosome_location_generated
 where sfclg_location_source = 'ZfinGbrowseZv9StartEndLoader';

delete from sequence_feature_chromosome_location_generated
 where sfclg_location_source = 'DirectSubmission';

delete from sequence_feature_chromosome_location_generated
 where sfclg_location_source = 'EnsemblStartEndLoader';

delete from sequence_feature_chromosome_location_generated
 where sfclg_location_source = 'UCSCStartEndLoader';

create temp table tmp_gff_start_end (accnum varchar(50),chrom varchar(20), gene varchar(50),
       start int,
       ender int
) ;

CREATE INDEX tmp_gff_start_end_acc ON tmp_gff_start_end (accnum);
CREATE INDEX tmp_gff_start_end_chrom ON tmp_gff_start_end (chrom);
CREATE INDEX tmp_gff_start_end_gene ON tmp_gff_start_end (gene);

insert into tmp_gff_start_end (accnum,chrom, gene)
select distinct gff_parent, gff_seqname, ensm_ensdarg_id
 from gff3, ensdar_mapping
 where gff_parent like 'ENSDART%'
and gff_parent = ensm_ensdart_id;

update tmp_gff_start_end
  set start = (select min(gff_start)
      	      	      from gff3
		      where gff_parent = accnum
		      and gff_seqname = chrom);

update tmp_gff_start_end
  set ender = (select max(gff_end)
      	      	      from gff3
		      where gff_parent = accnum
		      and gff_seqname = chrom);

select count(*) from tmp_gff_start_end
 where ender is null;

create index gene_index on tmp_gff_start_end (gene)
;
create index accnum_index on tmp_gff_start_end (accnum)
;
create temp table tmp_gene (accnum1 varchar(50) , chrom1 varchar(20), start int, ender int)
;

insert into tmp_gene (accnum1, chrom1)
 select distinct gene, chrom
  from tmp_gff_start_end;


create index gene2_index on tmp_gene (accnum1)
;
update tmp_gene
 set start = (select min(start)
      	      	      from tmp_gff_start_end
		      where  gene = accnum1 
		      and chrom = chrom1);


update tmp_gene
 set ender = (select max(ender)
      	      	      from tmp_gff_start_end
		      where  gene = accnum1 
		      and chrom = chrom1);

select count(*) from tmp_gene
 where start is null;

select count(*) from tmp_gene
 where ender is null;

create temp table tmp_ucsc (geneId text, 
				chrom1 varchar(10),
				accnum1 varchar(50),
				source text,
				fdb_db_pk_id int8)
;
insert into tmp_ucsc (geneId, chrom1, accnum1, source, fdb_db_pk_id)
select dblink_linked_recid, 
       chrom1,
		accnum1,
		'UCSCStartEndLoader',
		fdb_db_pk_id
  from db_link, tmp_gene, foreign_db, foreign_db_contains
  where dblink_Fdbcont_zdb_id = fdbcont_Zdb_id
  and dblink_acc_num = accnum1
  and fdb_db_pk_id = fdbcont_fdb_db_id
 and start is not null
 and ender is not null
;

create temp table tmp_ucsc_all (counter int,
			geneId text,
			chrom1 varchar(10),
			source text,
			subsource text,
			dblink_acc_num varchar(50));
insert into tmp_ucsc_all (counter, geneId, chrom1, source, subsource, dblink_acc_num)
select count(*) as counter, geneId, chrom1, source, source as subsource, dblink_acc_num
  from db_link, tmp_ucsc
 where geneId = dblink_linked_Recid
 and dblink_Acc_num like 'NM%'
 and exists (select 'x' from foreign_db_contains, foreign_db
     	    	    	where fdbcont_fdb_db_id = fdb_db_pk_id
			and fdb_db_name = 'RefSeq')
group by geneId, chrom1, source, dblink_acc_num
;
insert into sequence_feature_chromosome_location_generated (sfclg_data_Zdb_id, 
       	    			       sfclg_chromosome,
				       sfclg_acc_num,
				       sfclg_location_source,
				       sfclg_location_Subsource,
                                       sfclg_evidence_code
				   )
select distinct geneId, chrom1, dblink_acc_num, source, subsource, 'ZDB-TERM-170419-250'
  from tmp_ucsc_all;


insert into sequence_feature_chromosome_location_generated (sfclg_data_Zdb_id, 
       	    			       sfclg_chromosome,
				       sfclg_start,
				       sfclg_end,
				       sfclg_acc_num,
				       sfclg_location_source,
				       sfclg_fdb_db_id,sfclg_evidence_code)
select distinct dblink_linked_recid,
       		chrom1,
		start,
		ender,
		accnum1,
		'EnsemblStartEndLoader',
		fdb_db_pk_id, 'ZDB-TERM-170419-250'
  from db_link, tmp_gene, foreign_db, foreign_db_contains
  where dblink_Fdbcont_zdb_id = fdbcont_Zdb_id
  and dblink_acc_num = accnum1
  and fdb_db_pk_id = fdbcont_fdb_db_id
 and start is not null
 and ender is not null
    -- don't include fdb_db_display_name = 'ExpressionAtlas'
 and fdb_db_pk_id != 91
;

insert into sequence_feature_chromosome_location_generated (sfclg_data_Zdb_id, 
       	    			       sfclg_chromosome,
				       sfclg_start,
				       sfclg_end,
				       sfclg_acc_num,
				       sfclg_location_source,
				       sfclg_fdb_db_id,sfclg_evidence_code)
select dblink_linked_recid,
       		chrom1,
		start,
		ender,
		accnum1,
		'ZfinGbrowseStartEndLoader',
		fdb_db_pk_id, 'ZDB-TERM-170419-250'
  from db_link, tmp_gene, foreign_db, foreign_db_contains
  where dblink_Fdbcont_zdb_id = fdbcont_Zdb_id
  and dblink_acc_num = accnum1
  and fdb_db_pk_id = fdbcont_fdb_db_id
 and start is not null
 and ender is not null
 and exists (select 'x' from zfin_ensembl_gene where zeg_gene_zdb_id = dblink_linked_recid)
 group by dblink_linked_recid, chrom1, start, ender, accnum1, fdb_db_pk_id
;

insert into sequence_feature_chromosome_location_generated (
  sfclg_chromosome, sfclg_data_zdb_id, sfclg_start, sfclg_end, sfclg_location_source, sfclg_location_subsource, sfclg_assembly, sfclg_pub_zdb_id, sfclg_evidence_code)
select sfcl_chromosome, sfcl_feature_zdb_id, sfcl_start_position, sfcl_end_position, 'DirectSubmission', '', sfcl_assembly, recattrib_source_zdb_id, sfcl_evidence_code
  from sequence_feature_chromosome_location
  left outer join record_attribution on recattrib_data_zdb_id = sfcl_zdb_id;

update sequence_feature_chromosome_location_generated
set sfclg_gbrowse_track = 'zmp'
where sfclg_pub_zdb_id in ('ZDB-PUB-130425-4', 'ZDB-PUB-250905-18');

insert into sequence_feature_chromosome_location_generated (sfclg_chromosome, sfclg_data_zdb_id,
  sfclg_start, sfclg_end, sfclg_location_source, sfclg_location_subsource, sfclg_assembly, sfclg_gbrowse_track, sfclg_evidence_code)
select gff3.gff_seqname, feature.feature_zdb_id, gff3.gff_start, gff3.gff_end, 'ZfinGbrowseZv9StartEndLoader', 'BurgessLin', 'Zv9', 'insertion', 'ZDB-TERM-170419-250'
from gff3
inner join feature on (gff3.gff_id || 'Tg') = feature.feature_abbrev
where gff3.gff_source = 'BurgessLin';

insert into sequence_feature_chromosome_location_generated (sfclg_chromosome, sfclg_data_zdb_id,
  sfclg_start, sfclg_end, sfclg_location_source, sfclg_location_subsource, sfclg_evidence_code)
select gff_seqname, gff_name, gff_start, gff_end, 'ZfinGbrowseStartEndLoader', 'KnockdownReagentLoader', 'ZDB-TERM-170419-250'
from gff3
where gff_source = 'ZFIN_knockdown_reagent';


delete from sequence_feature_chromosome_location_generated
 where sfclg_chromosome in ('AB','U','0')
 and sfclg_location_source = 'UCSCStartEndLoader';

delete from sequence_feature_chromosome_location_generated
 where sfclg_chromosome in ('AB','U','0')
 and (
  sfclg_location_source = 'ZfinGbrowseStartEndLoader'
  OR sfclg_location_source = 'ZfinGbrowseZv9StartEndLoader'
);

delete from sequence_feature_chromosome_location_generated
 where sfclg_chromosome in ('AB','U','0')
 and sfclg_location_source = 'EnsemblStartEndLoader';

commit work;
-- commit or rollback is appended externally
--rollback work;commit work;

