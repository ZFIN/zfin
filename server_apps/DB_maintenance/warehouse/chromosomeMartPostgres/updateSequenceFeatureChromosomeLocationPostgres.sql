-- ============================================================================
-- Regenerate-Chromosome-Mart_d — runs downstream of Refresh-GBrowse-Tracks_d.
-- ============================================================================
-- The downstream-trigger wiring is in
-- server_apps/jenkins/jobs/Refresh-GBrowse-Tracks_d/config.xml
-- (hudson.tasks.BuildTrigger). The refresh job runs
-- server_apps/data_transfer/Ensembl/updateSequenceFeatureChromosomeLocation.sql,
-- which DELETE+INSERTs the bulk of UCSC, Ensembl, Zfin, and DirectSubmission
-- rows in sequence_feature_chromosome_location_generated. By the time this
-- script starts, the table is already in its post-refresh state, so this
-- script's only remaining work is the pieces the refresh path doesn't cover:
--
--   §E. ENSDARG-fallback EnsemblStartEndLoader rows for genes whose ENSDARG
--       accession has no ENSDART transcript link.
--   §H. zmp gbrowse-track tag (kept belt-and-suspenders for both ZMP pubs;
--       the refresh handles ZDB-PUB-130425-4 — re-tagging it here is a no-op
--       in steady state but acts as a fallback if refresh §H ever skips).
--
-- Section markers §A–§K below correspond to the same-named sections in the
-- refresh script. Sections marked "REDUNDANT — handled by refresh" are
-- commented out here; a follow-up PR will fully delete them.
-- ============================================================================

begin work;

-- ----------------------------------------------------------------------------
-- §A. REDUNDANCY REMOVED — handled by Refresh-GBrowse-Tracks_d §A
--     Bulk DELETE of the five sources we'd otherwise own here.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- §B. ACTIVE — build tmp_gff_start_end / tmp_gene from gff3 + ensdar_mapping.
--     Only tmp_gene.accnum1 is used downstream (by §E's "not in" filter); the
--     start / ender columns and the full structure here mirror refresh §B and
--     would be needed if any of §C / §D / §F were re-activated in this file.
-- ----------------------------------------------------------------------------
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

-- ----------------------------------------------------------------------------
-- §C. REDUNDANCY REMOVED — handled by Refresh-GBrowse-Tracks_d §C
--     UCSC: build tmp_ucsc / tmp_ucsc_all and INSERT 'UCSCStartEndLoader' rows.
--     The UCSC INSERT here was already disabled before this branch
--     (commit 1c246db2, ZFIN-9989) because the refresh path was the canonical
--     writer. The pruneUcscLinks.sh post-step in regenChromosomeMart.sh removes
--     any UCSC rows whose accession isn't in UCSC's danRer11 refGene track.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- §D. REDUNDANCY REMOVED — handled by Refresh-GBrowse-Tracks_d §D
--     EnsemblStartEndLoader main path: insert one row per gene/chromosome
--     using start/ender from §B.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- §E. ACTIVE — ENSDARG-fallback EnsemblStartEndLoader rows.
--     Warehouse-unique; no equivalent in refresh.sql. Introduced by ZFIN-9989
--     (PR #1619) for genes whose ENSDARG accession has no ENSDART transcript
--     link (so refresh §D's ENSDART path produces nothing for them).
--
--     The NOT EXISTS clause makes this idempotent — subsequent runs skip rows
--     already inserted. uk_sfclg_unique_location + ON CONFLICT doesn't help
--     here because that constraint covers sfclg_assembly and
--     sfclg_location_subsource, both NULL in fallback rows; PostgreSQL treats
--     NULLs in unique indexes as distinct, so the constraint doesn't fire on
--     rerun.
-- ----------------------------------------------------------------------------
insert into sequence_feature_chromosome_location_generated (sfclg_data_Zdb_id,
       	    			       sfclg_chromosome,
				       sfclg_start,
				       sfclg_end,
				       sfclg_acc_num,
				       sfclg_location_source,
				       sfclg_fdb_db_id,sfclg_evidence_code)
select distinct dblink_linked_recid,
       		gff_seqname,
		gff_start,
		gff_end,
		dblink_acc_num,
		'EnsemblStartEndLoader',
		fdb_db_pk_id, 'ZDB-TERM-170419-250'
  from db_link, foreign_db, foreign_db_contains, gff3
  where dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and dblink_acc_num = gff_id
  and fdb_db_pk_id = fdbcont_fdb_db_id
  and dblink_acc_num like 'ENSDARG%'
  and gff_feature = 'gene'
  -- Only include genes not already processed by the ENSDART logic in refresh §D
  and dblink_linked_recid not in (
    select distinct dblink_linked_recid
    from db_link, tmp_gene, foreign_db_contains
    where dblink_fdbcont_zdb_id = fdbcont_zdb_id
    and dblink_acc_num = accnum1
  )
  -- don't include fdb_db_display_name = 'ExpressionAtlas'
  and fdb_db_pk_id != 91
  -- skip rows we've already inserted (idempotence; see §E header)
  and not exists (
    select 1 from sequence_feature_chromosome_location_generated existing
     where existing.sfclg_data_zdb_id = dblink_linked_recid
       and existing.sfclg_location_source = 'EnsemblStartEndLoader'
       and existing.sfclg_acc_num = dblink_acc_num
       and existing.sfclg_chromosome = gff_seqname
       and existing.sfclg_start = gff_start
       and existing.sfclg_end = gff_end
  )
;

-- ----------------------------------------------------------------------------
-- §F. REDUNDANCY REMOVED — handled by Refresh-GBrowse-Tracks_d §F
--     ZfinGbrowseStartEndLoader from tmp_gene joined with zfin_ensembl_gene.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- §G. REDUNDANCY REMOVED — handled by Refresh-GBrowse-Tracks_d §G
--     DirectSubmission re-import from sequence_feature_chromosome_location.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- §H. ACTIVE — zmp gbrowse-track tag.
--     Refresh §H tags ZDB-PUB-130425-4. We re-tag both ZMP pubs here as
--     belt-and-suspenders: re-tagging ZDB-PUB-130425-4 is a no-op in steady
--     state, but acts as a fallback if refresh §H ever silently skips, and
--     ZDB-PUB-250905-18 is genuinely warehouse-unique work.
-- ----------------------------------------------------------------------------
update sequence_feature_chromosome_location_generated
set sfclg_gbrowse_track = 'zmp'
where sfclg_pub_zdb_id in ('ZDB-PUB-130425-4', 'ZDB-PUB-250905-18');

-- ----------------------------------------------------------------------------
-- §I. REDUNDANCY REMOVED — handled by Refresh-GBrowse-Tracks_d §I
--     BurgessLin Zv9 insertion features.
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- §J. REDUNDANCY REMOVED — handled by Refresh-GBrowse-Tracks_d §J
--     KnockdownReagentLoader (ZFIN_knockdown_reagent + GRCz12tu variant in
--     refresh; only the non-GRCz12tu variant was here historically).
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- §K. REDUNDANCY REMOVED — handled by Refresh-GBrowse-Tracks_d §K
--     Cleanup deletes for AB / U / 0 chromosomes across the source set.
-- ----------------------------------------------------------------------------

commit work;
-- commit or rollback is appended externally
--rollback work;commit work;
