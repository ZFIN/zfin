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


create temp table 
  tmp_gff_start_end (accnum varchar(50),chrom varchar(20), gene varchar(50),
       start int,
       endVal int
); 


insert into tmp_gff_start_end (accnum,chrom, gene)
select distinct gff_name, gff_seqname, ensm_ensdarg_id
 from gff3, ensdar_mapping
 where gff_name like 'ENSDART%'
and gff_name = ensm_ensdart_id;

update tmp_gff_start_end
  set start = (select min(gff_start)
      	      	      from gff3
		      where gff_name = accnum
		      and gff_seqname = chrom);

update tmp_gff_start_end

  set endVal = (select max(gff_end)
      	      	      from gff3
		      where gff_name = accnum
		      and gff_seqname = chrom);

select count(*) from tmp_gff_start_end
 where endVal is null;

create index gene_index on tmp_gff_start_end (gene);

create index accnum_index on tmp_gff_start_end (accnum);


create temp table tmp_gene (accnum1 varchar(50) , chrom1 varchar(20), start int, endVal int);

insert into tmp_gene (accnum1, chrom1)
 select distinct gene, chrom
  from tmp_gff_start_end;


create index gene2_index on tmp_gene (accnum1);


update tmp_gene
 set start = (select min(start)
      	      	      from tmp_gff_start_end
		      where  gene = accnum1 
		      and chrom = chrom1);


update tmp_gene
 set endVal = (select max(endVal)
      	      	      from tmp_gff_start_end
		      where  gene = accnum1 
		      and chrom = chrom1);



CREATE temp TABLE tmp_ucsc AS
  SELECT dblink_linked_recid          AS geneId,
    chrom1,
    accnum1,
    'UCSCStartEndLoader' :: text AS source,
    fdb_db_pk_id
  FROM   db_link,
    tmp_gene,
    foreign_db,
    foreign_db_contains
  WHERE  dblink_fdbcont_zdb_id = fdbcont_zdb_id
         AND dblink_acc_num = accnum1
         AND fdb_db_pk_id = fdbcont_fdb_db_id
         AND start IS NOT NULL
         AND endval IS NOT NULL;

create temp table tmp_ucsc_all as
select count(*) as counter, geneId, chrom1, source, source as subsource, dblink_acc_num
  from db_link, tmp_ucsc
 where geneId = dblink_linked_Recid
 and dblink_Acc_num like 'NM%'
 and exists (select 'x' from foreign_db_contains, foreign_db
     	    	    	where fdbcont_fdb_db_id = fdb_db_pk_id
			and fdb_db_name = 'RefSeq')
group by geneId, chrom1, source, dblink_acc_num;


insert into sequence_feature_chromosome_location_generated (sfclg_data_Zdb_id, 
       	    			       sfclg_chromosome,
				       sfclg_acc_num,
				       sfclg_location_source,
				       sfclg_location_Subsource
				   )
select distinct geneId, chrom1, dblink_acc_num, source, subsource
  from tmp_ucsc_all;



INSERT INTO sequence_feature_chromosome_location_generated
(sfclg_data_zdb_id,
 sfclg_chromosome,
 sfclg_start,
 sfclg_end,
 sfclg_acc_num,
 sfclg_location_source,
 sfclg_fdb_db_id)
  SELECT DISTINCT dblink_linked_recid,
    chrom1,
    start,
    endval,
    accnum1,
    'EnsemblStartEndLoader',
    fdb_db_pk_id
  FROM   db_link,
    tmp_gene,
    foreign_db,
    foreign_db_contains
  WHERE  dblink_fdbcont_zdb_id = fdbcont_zdb_id
         AND dblink_acc_num = accnum1
         AND fdb_db_pk_id = fdbcont_fdb_db_id
         AND start IS NOT NULL
         AND endval IS NOT NULL;

INSERT INTO sequence_feature_chromosome_location_generated
(sfclg_data_zdb_id,
 sfclg_chromosome,
 sfclg_start,
 sfclg_end,
 sfclg_acc_num,
 sfclg_location_source,
 sfclg_fdb_db_id)
  SELECT DISTINCT dblink_linked_recid,
    chrom1,
    start,
    endval,
    accnum1,
    'ZfinGbrowseStartEndLoader',
    fdb_db_pk_id
  FROM   db_link,
    tmp_gene,
    foreign_db,
    foreign_db_contains
  WHERE  dblink_fdbcont_zdb_id = fdbcont_zdb_id
         AND dblink_acc_num = accnum1
         AND fdb_db_pk_id = fdbcont_fdb_db_id
         AND start IS NOT NULL
         AND endval IS NOT NULL;

insert into sequence_feature_chromosome_location_generated (
  sfclg_chromosome, sfclg_data_zdb_id, sfclg_start, sfclg_end, sfclg_location_source, sfclg_location_subsource, sfclg_assembly, sfclg_pub_zdb_id)
select sfcl_chromosome, sfcl_feature_zdb_id, sfcl_start_position, sfcl_end_position, 'DirectSubmission', '', sfcl_assembly, recattrib_source_zdb_id
  from sequence_feature_chromosome_location
  left outer join record_attribution on recattrib_data_zdb_id = sfcl_zdb_id;

update sequence_feature_chromosome_location_generated
set sfclg_gbrowse_track = 'zmp'
where sfclg_pub_zdb_id = 'ZDB-PUB-130425-4';

insert into sequence_feature_chromosome_location_generated (sfclg_chromosome, sfclg_data_zdb_id,
  sfclg_start, sfclg_end, sfclg_location_source, sfclg_location_subsource, sfclg_assembly, sfclg_gbrowse_track)
select gff3.gff_seqname, feature.feature_zdb_id, gff3.gff_start, gff3.gff_end, 'ZfinGbrowseZv9StartEndLoader', 'BurgessLin', 'Zv9', 'insertion'
from gff3
inner join feature on (gff3.gff_id || 'Tg') = feature.feature_abbrev
where gff3.gff_source = 'BurgessLin';

insert into sequence_feature_chromosome_location_generated (sfclg_chromosome, sfclg_data_zdb_id,
  sfclg_start, sfclg_end, sfclg_location_source, sfclg_location_subsource)
select gff_seqname, gff_name, gff_start, gff_end, 'ZfinGbrowseStartEndLoader', 'KnockdownReagentLoader'
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

