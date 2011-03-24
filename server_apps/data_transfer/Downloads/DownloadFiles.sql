-- Script to create data files for public download.
--
-- We extract several different kinds of information:
--
-- All genetic markers (includes genes, ests, sslps, etc.)
--	zfin id, name, symbol, type, OR_lg
--
-- Synonyms  (for any item in all genetic markers file) There may be multiple lines
--   per zfin id
--	zfin id, synonym
--
-- Orthology - separate files for: D
--   zebrafish - human
--	zfin id , zebrafish symbol, human symbol, OMIM id, Entrez Gene id
--   zebrafish - mouse
--	zfin id , zebrafish symbol, mouse symbol, MGI id, Entrez Gene id
--   zebrafish - fly
--	zfin id,  zebrafish symbol, fly symbol,  Flybase id
--   zebrafish - yeast [5 stale records]
--	zfin id,  zebrafish symbol, yeast symbol,  SGD id
--
-- Gene Onotology-
--	A copy of the file we send to GO.
--
-- Gene Expression
--	gene zfin id , gene symbol, probe zfin id, probe name, expression type,
--      expression pattern zfin id, pub zfin id, genotype zfin id,
--      experiment zfin id
--
-- Mapping data
--	zfin id, symbol, panel symbol, LG, loc, metric
--
-- Sequence data - separate files for GenBank, RefSeq, Entrez Gene, Unigene,
-- UniProt, Interpro, GenPept and Vega (genes and transcripts) 1:1 Ensembl ID
-- as well as sequences indirectly associated with genes
--	zfin id, symbol, accession number
--
-- Genotypes
--	zfin id, allele/construct, type, gene symblol, corresponding zfin gene id
--
-- create genetic markers file
--
-- Morpholino data
--      zfin id of gene, gene symbol, zfin id of MO, MO symbol, public note
-- Marker Relationship data
--	marker1 id, marker1 symbol, marker 2 id, marker 2 symbol, relationship

-- create antibody download file
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibodies.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibodies.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev, atb_type, atb_hviso_name, atb_ltiso_name,
	atb_immun_organism, atb_host_organism
  from marker, antibody
 where mrkr_zdb_id = atb_zdb_id
 order by 1;

-- create antibody expression download file
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibody_expressions.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/antibody_expressions.txt'
 DELIMITER "	"
select distinct mrkr_zdb_id, super.term_ont_id, super.term_name, sub.term_ont_id, sub.term_name
 from marker, expression_experiment, expression_result, term as super,
      outer term as sub, genotype_experiment, experiment, genotype
 where xpatres_xpatex_zdb_id = xpatex_zdb_id
   AND xpatex_atb_zdb_id = mrkr_zdb_id
   AND mrkr_type = 'ATB'
   AND super.term_zdb_id = xpatres_superterm_zdb_id
   AND sub.term_zdb_id = xpatres_subterm_zdb_id
   AND xpatex_genox_zdb_id = genox_zdb_id
   AND genox_exp_zdb_id = exp_zdb_id
   AND (exp_name = '_Standard' OR exp_name = '_Generic-control')
   AND xpatres_expression_found = 't'
   AND geno_zdb_id = genox_geno_zdb_id
   AND geno_is_wildtype = 't'
 order by mrkr_zdb_id
;

-- create all marker file
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genetic_markers.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genetic_markers.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev, mrkr_name, mrkr_type
 from marker order by 1;
-- create other names file

create temp table tmp_alias (current_id varchar(50), current_name varchar(255),
       current_abbrev varchar(255), alias varchar(255))
with no log;

  insert into tmp_alias
  select mrkr_zdb_id as current_id, mrkr_name as current_name,
		mrkr_abbrev as current_abbrev, dalias_alias as alias
    from marker, data_alias
    where dalias_data_zdb_id = mrkr_zdb_id;

   insert into tmp_alias
   select feature_zdb_id, feature_name,feature_abbrev, dalias_alias
    from feature, data_alias
    where feature_zdb_id = dalias_data_zdb_id;

   insert into tmp_alias
   select geno_zdb_id, geno_display_name, geno_handle, dalias_alias
    from genotype, data_alias
    where dalias_data_zdb_id = geno_Zdb_id;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/aliases.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/aliases.txt'
  DELIMITER "	"
select mrkr_zdb_id , mrkr_name, mrkr_abbrev, dalias_alias
 from marker, data_alias
 where dalias_data_zdb_id = mrkr_zdb_id
union
select feature_zdb_id, feature_name,feature_abbrev, dalias_alias
 from feature, data_alias
 where feature_zdb_id = dalias_data_zdb_id
union
select geno_zdb_id, geno_display_name, geno_handle, dalias_alias
 from genotype, data_alias
 where dalias_data_zdb_id = geno_Zdb_id
 order by 1, 4
;

-- Create marker realtionship file
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/gene_marker_relationship.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/gene_marker_relationship.txt'
 DELIMITER "	"
select gene.mrkr_zdb_id, gene.mrkr_abbrev, seq.mrkr_zdb_id, seq.mrkr_abbrev, mrel_type
 from marker_relationship, marker gene, marker seq
 where gene.mrkr_type[1,4] = 'GENE'
   and seq.mrkr_type[1,4] != 'GENE'
   and mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and mrel_mrkr_2_zdb_id = seq.mrkr_zdb_id
union
select gene.mrkr_zdb_id, gene.mrkr_abbrev, seq.mrkr_zdb_id, seq.mrkr_abbrev, mrel_type
 from marker_relationship, marker gene, marker seq
 where gene.mrkr_type[1,4] = 'GENE'
   and seq.mrkr_type[1,4] != 'GENE'
   and mrel_mrkr_2_zdb_id = gene.mrkr_zdb_id
   and mrel_mrkr_1_zdb_id = seq.mrkr_zdb_id
;

-- Create the orthologues files - mouse, human, fly and yeast
create temp table tmp_ortho_exp (
  gene_id varchar(50),
  ortho_id varchar(50),
  zfish_name varchar(120),
  zfish_abbrev varchar(40),
  organism varchar(30),
  ortho_name varchar(120),
  ortho_abbrev varchar(15),
  flybase varchar(50),
  entrez varchar(50),
  mgi varchar(50),
  omim varchar(50),
  sgd varchar(50)
) with no log;

insert into tmp_ortho_exp
select distinct c_gene_id, zdb_id, mrkr_name, mrkr_abbrev, organism, ortho_name,ortho_abbrev,
	NULL::varchar(50),NULL::varchar(50),NULL::varchar(50),NULL::varchar(50),NULL::varchar(50)
 from orthologue,marker
 where c_gene_id = mrkr_zdb_id
;

update tmp_ortho_exp set flybase = (
	select distinct dblink_acc_num
	 from db_link, orthologue o, foreign_db_contains, foreign_db
	 where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	   and fdb_db_name = 'FLYBASE'
	   and fdbcont_fdb_db_id = fdb_db_pk_id
	   and fdbcont_organism_common_name = o.organism
	   and o.zdb_id = dblink_linked_recid
	   and ortho_id = o.zdb_id
);

update tmp_ortho_exp set Entrez = (
	select dblink_acc_num
	 from db_link, orthologue o, foreign_db_contains, foreign_db
	 where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	   and fdb_db_name = 'Entrez Gene'
	   and fdbcont_fdb_db_id = fdb_db_pk_id
	   and fdbcont_organism_common_name = o.organism
	   and o.zdb_id = dblink_linked_recid
	   and ortho_id = o.zdb_id
);

update tmp_ortho_exp set mgi = (
	select 'MGI:' || dblink_acc_num
	 from db_link , orthologue o, foreign_db_contains, foreign_db
	 where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	   and fdbcont_fdb_db_id = fdb_db_pk_id
	   and fdb_db_name = 'MGI'
	   and fdbcont_organism_common_name = o.organism
	   and o.zdb_id = dblink_linked_recid
	   and ortho_id = o.zdb_id
);

update tmp_ortho_exp set omim = (
	select distinct dblink_acc_num
	 from db_link, orthologue o, foreign_db_contains, foreign_db
	 where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	   and fdbcont_fdb_db_id = fdb_db_pk_id
	   and fdb_db_name = 'OMIM'
	   and fdbcont_organism_common_name = o.organism
	   and o.zdb_id = dblink_linked_recid
	   and ortho_id = o.zdb_id
);

update tmp_ortho_exp set sgd = (
	select dblink_acc_num
	 from db_link, orthologue o, foreign_db_contains, foreign_db
	 where dblink_fdbcont_zdb_id = fdbcont_zdb_id
	   and fdb_db_name = 'SGD'
	   and fdbcont_fdb_db_id = fdb_db_pk_id
	   and fdbcont_organism_common_name = o.organism
	   and o.zdb_id = dblink_linked_recid
	   and ortho_id = o.zdb_id
);

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/fly_orthos.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/fly_orthos.txt'
 DELIMITER "	"
select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, flybase
 from tmp_ortho_exp
 where organism = 'Fruit fly'
 order by 1;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/human_orthos.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/human_orthos.txt'
 DELIMITER "	"
select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, omim, entrez
 from tmp_ortho_exp
 where organism = 'Human'
 order by 1;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mouse_orthos.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mouse_orthos.txt'
 DELIMITER "	"
select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, mgi, entrez
 from tmp_ortho_exp
 where organism = 'Mouse'
 order by 1;

-- going away shortly
--! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/yeast_orthos.txt'"
--UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/yeast_orthos.txt'
--  DELIMITER "	"
--select gene_id, zfish_abbrev, zfish_name, ortho_abbrev, ortho_name, sgd
-- from tmp_ortho_exp
-- where organism = 'Yeast'
-- order by 1;

drop table tmp_ortho_exp;

-- generate a file with genes and associated expression experiment
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat.txt'
 DELIMITER "	"
select gene.mrkr_zdb_id gene_zdb, gene.mrkr_abbrev,
        probe.mrkr_zdb_id probe_zdb, probe.mrkr_abbrev,
        xpatex_assay_name, xpatex_zdb_id xpat_zdb,
        xpatex_source_zdb_id,
        genox_geno_zdb_id, genox_exp_zdb_id,
        clone_rating
 from expression_experiment
 join genotype_experiment
   on genox_zdb_id = xpatex_genox_zdb_id
 join marker gene
   on gene.mrkr_zdb_id = xpatex_gene_zdb_id
 left join marker probe
   on probe.mrkr_zdb_id = xpatex_probe_feature_zdb_id
 left join clone
   on clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
 where gene.mrkr_abbrev[1,11] != 'WITHDRAWN: ' -- Xiang noticed this misses those without a space after :
   and exists (
	select 1 from expression_result
	 where xpatres_xpatex_zdb_id = xpatex_zdb_id
 ) order by gene_zdb, xpat_zdb, probe_zdb
;

-- generate a file with antibodies and associated expression experiment
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/abxpat.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/abxpat.txt'
 DELIMITER "	"
 select antibody.atb_zdb_id, atb.mrkr_abbrev,gene.mrkr_zdb_id as gene_zdb,
	gene.mrkr_abbrev, xpatex_assay_name, xpatex_zdb_id as xpat_zdb,
	xpatex_source_zdb_id, genox_geno_zdb_id, genox_exp_zdb_id
 from expression_experiment
 join genotype_experiment
   on genox_zdb_id = xpatex_genox_zdb_id
 join antibody antibody
   on antibody.atb_zdb_id = xpatex_atb_zdb_id
 left join marker gene
   on gene.mrkr_zdb_id = xpatex_gene_zdb_id, marker as atb
 where atb.mrkr_zdb_id  = antibody.atb_zdb_id
   and xpatex_gene_zdb_id is null
UNION
 select antibody.atb_zdb_id, atb.mrkr_abbrev, gene.mrkr_zdb_id as gene_zdb,
	gene.mrkr_abbrev, xpatex_assay_name, xpatex_zdb_id as xpat_zdb,
	xpatex_source_zdb_id, genox_geno_zdb_id, genox_exp_zdb_id
 from expression_experiment
 join genotype_experiment
   on genox_zdb_id = xpatex_genox_zdb_id
 join antibody antibody
   on antibody.atb_zdb_id = xpatex_atb_zdb_id
 join marker gene
   on gene.mrkr_zdb_id = xpatex_gene_zdb_id, marker as atb
 where gene.mrkr_abbrev[1,11] != 'WITHDRAWN: '
   and atb.mrkr_zdb_id  = antibody.atb_zdb_id
 order by antibody.atb_zdb_id, xpat_zdb
;

-- generate a file to map experiment id to environment condition description
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat_environment.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat_environment.txt'
 DELIMITER "	"
select exp_zdb_id, cdt_group,
 case when expcond_mrkr_zdb_id is not null
	then expcond_mrkr_zdb_id
	else cdt_name
 end, expcond_value, expunit_name, expcond_comments
 from experiment, experiment_condition, condition_data_type, experiment_unit
 where exp_zdb_id = expcond_exp_zdb_id
   and expcond_cdt_zdb_id = cdt_zdb_id
   and expcond_expunit_zdb_id = expunit_zdb_id
   and exists (
	select 't' from genotype_experiment, expression_experiment
	 where exp_zdb_id = genox_exp_zdb_id
	   and genox_zdb_id = xpatex_genox_zdb_id
)
-- special handling for _Generic-control ;;insert into tmp_env
union
select exp_zdb_id, exp_name, exp_name, "N/A", "N/A", "This environment is used for non-standard conditions used in control treatments."
 from experiment
 where exp_name = "_Generic-control"
 order by  1,2
;


-- generate a file with genes and associated expression experiment
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/phenotype.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/phenotype.txt'
 DELIMITER "	"
 select distinct g.geno_zdb_id, g.geno_display_name,
            phenox_start_stg_zdb_id,
            (select stg_name
                from stage
                where stg_zdb_id = phenox_start_stg_zdb_id),
            phenox_end_stg_zdb_id,
            (select stg_name from stage where stg_zdb_id = phenox_end_stg_zdb_id),
              (select term_ont_id from term where term_zdb_id = phenos_entity_1_superterm_zdb_id),
            (select term_name from term where term_zdb_id = phenos_entity_1_superterm_zdb_id),
              (select term_ont_id from term where term_zdb_id = phenos_entity_1_subterm_zdb_id),
            (select term_name from term where term_zdb_id = phenos_entity_1_subterm_zdb_id),
              (select term_ont_id from term where term_zdb_id = phenos_quality_zdb_id),
            (select term_name from term where term_zdb_id = phenos_quality_zdb_id),
            phenos_tag,
              (select term_ont_id from term where term_zdb_id = phenos_entity_2_superterm_zdb_id),
            (select term_name from term where term_zdb_id = phenos_entity_2_superterm_zdb_id),
              (select term_ont_id from term where term_zdb_id = phenos_entity_2_subterm_zdb_id),
            (select term_name from term where term_zdb_id = phenos_entity_2_subterm_zdb_id),
            fig_source_zdb_id,
            gx.genox_exp_zdb_id
  from phenotype_experiment, phenotype_statement, figure, genotype g, genotype_experiment gx
 where phenos_phenox_pk_id = phenox_pk_id
   and phenox_genox_zdb_id = gx.genox_zdb_id
   and gx.genox_geno_zdb_id = g.geno_zdb_id
   and phenox_fig_zdb_id = fig_zdb_id
 order by geno_zdb_id, fig_source_zdb_id;

! echo "Inserted data into file phenotype.txt"

-- generate a file with xpatex and associated figure zdbid's
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpatfig.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpatfig.txt'
 DELIMITER "	"
select distinct xpatex_zdb_id, xpatres_zdb_id, xpatfig_fig_zdb_id
 from expression_experiment, expression_result,expression_pattern_figure
 where xpatex_zdb_id=xpatres_xpatex_zdb_id
   and xpatres_zdb_id=xpatfig_xpatres_zdb_id
 order by xpatex_zdb_id;


-- generate a file with genotype id's and associated figure zdbid's
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genofig.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genofig.txt'
 DELIMITER "	"
 select distinct genox_geno_zdb_id, phenox_fig_zdb_id
 from genotype_experiment, phenotype_experiment
 where genox_zdb_id = phenox_genox_zdb_id
 order by genox_geno_zdb_id;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pheno_obo.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pheno_obo.txt'
 DELIMITER "	"
select "ZFIN:"||geno_zdb_id, geno_display_name,
			(select stg_obo_id from stage 
			   where stg_zdb_id = phenox_start_stg_zdb_id),
			(select stg_obo_id from stage
                           where stg_zdb_id = phenox_end_stg_zdb_id),
                (select term_ont_id
                    from term
                    where term_zdb_id = phenos_entity_1_superterm_zdb_id
                ),
                (select term_ont_id
                    from term
                    where term_zdb_id = phenos_entity_1_subterm_zdb_id
                ),
                (select term_ont_id
                    from term
                    where term_Zdb_id = phenos_quality_zdb_id),
			phenos_tag,
                (select term_ont_id
                    from term
                    where term_zdb_id = phenos_entity_2_superterm_zdb_id
                ),
                (select term_ont_id
                    from term
                    where term_zdb_id = phenos_entity_2_subterm_zdb_id
                ),
			"ZFIN:"||fig_source_zdb_id,
			"ZFIN:"||genox_exp_zdb_id
  from phenotype_experiment, phenotype_statement, figure, genotype, genotype_experiment
 where phenox_genox_zdb_id = genox_zdb_id
   and phenos_phenox_pk_id = phenox_pk_id
   and phenox_fig_zdb_id = fig_zdb_id
   and genox_geno_zdb_id = geno_zdb_id
 order by geno_zdb_id, fig_source_zdb_id ;


-- generate a file to map experiment id to environment condition description

insert into tmp_env
 select exp_zdb_id, cdt_group,
        case when expcond_mrkr_zdb_id is not null
             then expcond_mrkr_zdb_id
	     else cdt_name
 	end, expcond_value, expunit_name, expcond_comments
   from experiment, experiment_condition, condition_data_type, experiment_unit
  where exp_zdb_id = expcond_exp_zdb_id
    and expcond_cdt_zdb_id = cdt_zdb_id
    and expcond_expunit_zdb_id = expunit_zdb_id
    and exists (select 't'
                  from genotype_experiment, phenotype_experiment
                 where exp_zdb_id = genox_exp_zdb_id
                   and genox_zdb_id = phenox_genox_zdb_id);

-- special handling for _Generic-control
insert into tmp_env
 select exp_zdb_id, exp_name, exp_name, "N/A", "N/A", "This environment is used for non-standard conditions used in control treatments."
   from experiment
  where exp_name = "_Generic-control";

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pheno_environment.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pheno_environment.txt'
 DELIMITER "	"
select exp_zdb_id, cdt_group,
 case when expcond_mrkr_zdb_id is not null
	then expcond_mrkr_zdb_id
	else cdt_name
 end, expcond_value, expunit_name, expcond_comments
 from experiment, experiment_condition, condition_data_type, experiment_unit
 where exp_zdb_id = expcond_exp_zdb_id
   and expcond_cdt_zdb_id = cdt_zdb_id
   and expcond_expunit_zdb_id = expunit_zdb_id
   and exists (
	select 't'
	 from genotype_experiment, atomic_phenotype
	 where exp_zdb_id = genox_exp_zdb_id
	   and genox_zdb_id = apato_genox_zdb_id
)
union
-- special handling for _Generic-control--insert into tmp_env
select exp_zdb_id, exp_name, exp_name, "N/A", "N/A", "This environment is used for non-standard conditions used in control treatments."
 from experiment
 where exp_name = "_Generic-control"
 order by 1,2
;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pub_to_pubmed_id_translation.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pub_to_pubmed_id_translation.txt'
 DELIMITER "	" select zdb_id, accession_no from publication ;

-- Create mapping data file
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mappings.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/mappings.txt'
 DELIMITER "	"
select marker_id, mrkr_abbrev, p.abbrev,or_lg, lg_location, p.metric
 from mapped_marker, panels p, marker m
 where refcross_id = p.zdb_id and marker_id = mrkr_zdb_id
 order by 1;

-- Generate sequence data files for GenBank, RefSeq, Entrez, UniGene, UniProt, Interpro and GenPept

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genbank.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genbank.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev, dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db
 where mrkr_zdb_id = dblink_linked_recid
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdb_db_name = 'GenBank'
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and dblink_acc_num[3] <> "_"  -- filter misplaced acc
   and not (
   	dblink_linked_recid[1,12] = 'ZDB-TSCRIPT-'
   	and dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-37"
 ) order by 1;

-- the last condition is added to filter out mis-placed acc
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/refseq.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/refseq.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db
 where mrkr_zdb_id = dblink_linked_recid
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdb_db_name = 'RefSeq'
   and dblink_acc_num[3] = "_"
 order by 1;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/entrezgene.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/entrezgene.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db
 where mrkr_zdb_id = dblink_linked_recid
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdb_db_name = 'Entrez Gene'
 order by 1;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/unigene.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/unigene.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db
 where mrkr_zdb_id = dblink_linked_recid
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdb_db_name = 'UniGene'
order by 1;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/uniprot.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/uniprot.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db
 where mrkr_zdb_id = dblink_linked_recid
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and fdb_db_name = 'UniProtKB'
order by 1;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/interpro.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/interpro.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db
 where mrkr_zdb_id = dblink_linked_recid
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdb_db_name = 'InterPro'
order by 1;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pfam.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/pfam.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db
 where mrkr_zdb_id = dblink_linked_recid
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdb_db_name = 'Pfam'
order by 1;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genpept.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genpept.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains, foreign_db
 where mrkr_zdb_id = dblink_linked_recid
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdbcont_zdb_id = dblink_fdbcont_zdb_id
   and fdb_db_name = 'GenPept'
 order by 1;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega.txt'
 DELIMITER "	"
select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db
 where mrkr_zdb_id = dblink_linked_recid
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdbcont_zdb_id = dblink_fdbcont_zdb_id
   and fdb_db_name in ('VEGA','unreleasedRNA')
 order by 1;

-- vega_transcript.txt is only used by tomc and Sanger.
-- please check before changing thanks.

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega_transcript.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/vega_transcript.txt'
 DELIMITER "	"
select distinct mrkr_zdb_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, marker_relationship
 where mrel_type = 'gene produces transcript'
   and dblink_acc_num[1,8] = 'OTTDART0'
   and mrel_mrkr_2_zdb_id ==dblink_linked_recid
   and mrel_mrkr_1_zdb_id = mrkr_zdb_id
   and not (
   	dblink_linked_recid[1,12] = 'ZDB-TSCRIPT-'
   	and dblink_fdbcont_zdb_id = "ZDB-FDBCONT-040412-37"
   ) order by 1
;

-- the changing assembly version number in db_name
-- is apt to come back to bite us so I am opting for the zdb_id
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/ensembl_1_to_1.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/ensembl_1_to_1.txt'
 DELIMITER "	"
select  "#    ZDBID          ","SYMBOL", fdb_db_name
 from foreign_db_contains, foreign_db
 where fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
   and fdbcont_fdb_db_id = fdb_db_pk_id
union
select mrkr_zdb_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link
 where mrkr_zdb_id = dblink_linked_recid
   and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
 order by 1;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/all_rna_accessions.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/all_rna_accessions.txt'
select distinct gene.mrkr_zdb_id gene_zdb, gene.mrkr_abbrev gene_sym,dblink_acc_num accession
 from db_link, marker gene, foreign_db_contains, foreign_db, foreign_db_data_type
 where dblink_linked_recid=gene.mrkr_zdb_id
   and gene.mrkr_type='GENE'
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdbdt_id = fdbdt_pk_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdbdt_data_type='RNA'
   -- and fdb_db_name not like '%Vega%' and fdb_db_name not like '%VEGA%'
   -- note: the below would have the same effect as above
   --       but since neither catch anything go with the faster/cheaper
   and dblink_acc_num[1,6] != "OTTDAR"
union
select distinct gene.mrkr_zdb_id gene_zdb,
       gene.mrkr_abbrev gene_sym,
       dblink_acc_num accession
 from marker gene, marker est, db_link, marker_relationship, foreign_db_contains,
 	foreign_db, foreign_db_data_type
 where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id
   and est.mrkr_zdb_id  = mrel_mrkr_2_zdb_id
   and mrel_type = 'gene encodes small segment'
   and est.mrkr_zdb_id = dblink_linked_recid
   and est.mrkr_type  in ('EST','CDNA')
   and gene.mrkr_type = 'GENE'
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdbdt_id = fdbdt_pk_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdbdt_data_type='RNA'
   -- and fdb_db_name not like '%Vega%' and fdb_db_name not like '%VEGA%'
   -- note: the below would have the same effect as above
   --       but since neither catch anything go with the faster/cheaper
   and dblink_acc_num[1,6] != "OTTDAR"
;

-- Generate genotype_feature file

create temp table tmp_geno_data (
  genotype_id varchar(50),
  geno_display_name varchar(255),
  geno_handle varchar(255),
  feature_name varchar(255),
  feature_abbrev varchar(30),
  feature_type varchar(30),
  feature_type_display varchar(40),
  gene_abbrev varchar(40),
  gene_id varchar(50),
  feature_zdb_id varchar(50)
) with no log ;

insert into tmp_geno_data (
	genotype_id, geno_display_name, geno_handle, feature_name, feature_abbrev,
	feature_type, feature_type_display,feature_zdb_id
)
select
	genofeat_geno_zdb_id,
	geno_display_name,
	geno_handle,
	feature_name,
	feature_abbrev,
	lower(feature_type),
	ftrtype_type_display,
	feature_zdb_id
 from genotype_feature, feature, genotype, feature_type
 where genofeat_feature_zdb_id = feature_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
   and feature_type = ftrtype_name
;

update tmp_geno_data set (gene_id, gene_abbrev) = ((
	select mrkr_zdb_id, mrkr_abbrev
	 from marker, feature_marker_relationship, feature
	 where tmp_geno_data.feature_name = feature_name
	   and fmrel_ftr_zdb_id = feature_zdb_id
	   and fmrel_mrkr_zdb_id = mrkr_zdb_id
	   and fmrel_type = "is allele of"
));

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genotype_features.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genotype_features.txt'
 DELIMITER "	"
select distinct
	genotype_id,
	geno_display_name,
	geno_handle,
	feature_zdb_id,
	feature_name,
	feature_abbrev,
	feature_type,
	feature_type_display,
	gene_abbrev,
	gene_id
 from tmp_geno_data
 order by genotype_id, geno_display_name
;

drop table tmp_geno_data;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genotype_features_missing_markers.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genotype_features_missing_markers.txt'
 DELIMITER "	"
select distinct  geno_zdb_id, geno_display_name, geno_handle, mrkr_abbrev, mrkr_zdb_id
 from feature_marker_relationship, feature, genotype, genotype_feature, marker
 where fmrel_ftr_zdb_id=feature_zdb_id
   and fmrel_mrkr_zdb_id=mrkr_zdb_id
   and fmrel_type in ('markers missing', 'markers moved')
   and mrkr_zdb_id[1,8] = 'ZDB-GENE'
   and feature_zdb_id = genofeat_feature_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genotype_backgrounds.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genotype_backgrounds.txt'
 DELIMITER "	"
select distinct geno_zdb_id, geno_display_name, genoback_background_zdb_id
 from genotype, genotype_background
 where geno_Zdb_id = genoback_geno_Zdb_id
;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/wildtypes.tx'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/wildtypes.txt'
 DELIMITER "	"
select distinct geno_zdb_id, geno_display_name, geno_handle
 from genotype
 where geno_is_wildtype = 't'
;

-- generate a file with zdb history data
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zdb_history.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zdb_history.txt'
 DELIMITER "	" select zrepld_old_zdb_id, zrepld_new_zdb_id from zdb_replaced_data;

-- indirect sequence links for genes

select distinct gene.mrkr_zdb_id gene_zdb,
       gene.mrkr_abbrev gene_sym,
       dblink_acc_num genbank_acc
from marker gene, marker est, db_link, marker_relationship,foreign_db, foreign_db_contains
 where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id
   and   est.mrkr_zdb_id  = mrel_mrkr_2_zdb_id
   and  mrel_type = 'gene encodes small segment'
   and est.mrkr_zdb_id = dblink_linked_recid
   and est.mrkr_type  in ('EST','CDNA')
   and gene.mrkr_type = 'GENE'
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdb_db_name = 'GenBank'
   and fdbcont_fdb_db_id = fdb_db_pk_id
 into temp tmp_veg with no log;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/gene_seq.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/gene_seq.txt'
 DELIMITER "	" select * from tmp_veg order by 1,3;
drop table tmp_veg;

-- Anatomical Ontologies
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_ontology.txt'"
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_ontology.txt'
 DELIMITER "	"
select anatrel_anatitem_1_zdb_id, anatrel_anatitem_2_zdb_id
 from anatomy_relationship
;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/stage_ontology.txt'"
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/stage_ontology.txt'
 DELIMITER "	"
select stg_zdb_id, stg_obo_id, stg_name, stg_hours_start, stg_hours_end
  from stage
  order by stg_hours_start, stg_hours_end desc
;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_item.txt'"
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_item.txt'
 DELIMITER "	"
select term_ont_id, term_name, anatitem_start_stg_zdb_id, anatitem_end_stg_zdb_id
 from term, anatomy_item
 where term_ont_id = anatitem_obo_id
;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_relationship.txt'"
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_relationship.txt'
DELIMITER "	"
select term1.term_ont_id, term2.term_ont_id, termrel_type
 from term_relationship, term as term1, term as term2
 where term1.term_ontology = 'zebrafish_anatomy'
   and term2.term_ontology = 'zebrafish_anatomy'
   and term1.term_zdb_id = termrel_term_1_zdb_id
   and term2.term_zdb_id = termrel_term_2_zdb_id
;


! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat_stage_anatomy.txt'"
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/xpat_stage_anatomy.txt'
 DELIMITER "	"
select xpatres_zdb_id,
       xpatres_xpatex_zdb_id,
       xpatres_start_stg_zdb_id,
       xpatres_end_stg_zdb_id,
       superterm.term_ont_id,
       subterm.term_ont_id,
       xpatres_expression_found
 from expression_result, term superterm, OUTER term subterm -- not snappy
 where superterm.term_zdb_id = xpatres_superterm_zdb_id
   and subterm.term_zdb_id = xpatres_subterm_zdb_id
 order by xpatres_xpatex_zdb_id
;

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_synonyms.txt'"
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/anatomy_synonyms.txt'
 DELIMITER "	"
select dalias_data_zdb_id, anatitem_name, dalias_alias
 from data_alias, anatomy_item, alias_group
 where dalias_data_zdb_id = anatitem_zdb_id
   and dalias_data_zdb_id[1,8] = 'ZDB-ANAT'
   and dalias_alias[1,4] != 'ZFA:'
   and dalias_group_id = aliasgrp_pk_id
   and aliasgrp_name not in ('plural','secondary id')
 order by anatitem_name
;

-- Morpholino data
-- unloaded Morpholino data would have HTML tags in public note column,
-- which will be removed by Perl script
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/Morpholinos2.txt'"
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/Morpholinos2.txt'
 DELIMITER "	"
select gn.mrkr_zdb_id, gn.mrkr_abbrev, mo.mrkr_zdb_id, mo.mrkr_abbrev,
	mrkrseq_sequence, mo.mrkr_comments
 from marker gn, marker mo, marker_sequence, marker_relationship
 where gn.mrkr_zdb_id = mrel_mrkr_2_zdb_id
   and mo.mrkr_zdb_id = mrel_mrkr_1_zdb_id
   and mrel_mrkr_2_zdb_id[1,9] = "ZDB-GENE-" -- note ommits pseudogenes, hope that was deliberate
   and mrel_mrkr_1_zdb_id[1,12] = "ZDB-MRPHLNO-"
   and mrel_type = "knockdown reagent targets gene"
   and mo.mrkr_zdb_id = mrkrseq_mrkr_zdb_id
   order by gn.mrkr_abbrev
;

-- Image data
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/ImageFigures.txt'"
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/ImageFigures.txt'
 DELIMITER "	"
select img_zdb_id, img_fig_zdb_id, img_preparation
 from image
 where img_fig_zdb_id is not null
 order by img_zdb_id;

-- Transcript data
-- Get clones and genes if available but still report if not (a small subset)
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/transcripts.txt'"
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/transcripts.txt'
 DELIMITER "	"
select t.tscript_mrkr_zdb_id,m.mrkr_name,'no related gene','no related clone',tt.tscriptt_type,ts.tscripts_status
 from transcript t
 join marker m on m.mrkr_zdb_id=t.tscript_mrkr_zdb_id
 join transcript_type tt on tt.tscriptt_pk_id=t.tscript_type_id
 left outer join transcript_status ts on ts.tscripts_pk_id=t.tscript_status_id
 where not exists (
	select 'x'
	 from marker gene , marker_relationship mr
	 where gene.mrkr_zdb_id=mr.mrel_mrkr_1_zdb_id
	   and mr.mrel_mrkr_2_zdb_id=m.mrkr_zdb_id
	   and mr.mrel_type in ('gene produces transcript','clone contains transcript')
)
union
select t.tscript_mrkr_zdb_id,m.mrkr_name,gene.mrkr_zdb_id,c.mrkr_zdb_id,tt.tscriptt_type,ts.tscripts_status
 from transcript t
 join marker m on m.mrkr_zdb_id=t.tscript_mrkr_zdb_id
 join transcript_type tt on tt.tscriptt_pk_id=t.tscript_type_id
 left outer join transcript_status ts on ts.tscripts_pk_id=t.tscript_status_id
 join marker_relationship gener on gener.mrel_mrkr_2_zdb_id=m.mrkr_zdb_id
 join marker_relationship cloner on cloner.mrel_mrkr_2_zdb_id=m.mrkr_zdb_id
 join marker gene on gene.mrkr_zdb_id=gener.mrel_mrkr_1_zdb_id
 join marker c on c.mrkr_zdb_id=cloner.mrel_mrkr_1_zdb_id
 where gener.mrel_type='gene produces transcript'
   and cloner.mrel_type='clone contains transcript'
;

-- unload publication - genotype association file
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genotype_publication.txt'"
unload to  '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/genotype_publication.txt'
 DELIMITER "	"
select recattrib_data_zdb_id as genotype_zdb_id, recattrib_Source_zdb_id as pub_zdb_id
 from record_attribution, genotype
 where recattrib_data_zdb_id = geno_zdb_id
   and recattrib_source_type = 'standard'
;

-- create full expression file for WT fish: standard condition, expression shown and
-- only wildtype fish
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/wildtype-expression.txt'"
unload to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/wildtype-expression.txt'
 DELIMITER "	"

select mrkr_zdb_id, mrkr_abbrev, geno_display_name, super.term_ont_id, super.term_name, sub.term_ont_id, sub.term_name, 
startStage.stg_name, endStage.stg_name, xpatex_assay_name
from marker
join expression_experiment on xpatex_gene_zdb_id = mrkr_zdb_id
join genotype_experiment on genox_zdb_id = xpatex_genox_zdb_id
join genotype on geno_zdb_id = genox_geno_zdb_id
join experiment on exp_zdb_id = genox_exp_zdb_id
join expression_result on xpatres_xpatex_zdb_id = xpatex_zdb_id
join stage startStage on xpatres_start_stg_zdb_id = startStage.stg_zdb_id 
join stage endStage on xpatres_end_stg_zdb_id = endStage.stg_zdb_id 
join term super on xpatres_superterm_zdb_id = super.term_zdb_id
left outer join term sub on xpatres_subterm_zdb_id = sub.term_zdb_id
where     geno_is_wildtype = 't'
      and (exp_zdb_id = 'ZDB-EXP-041102-1' or exp_zdb_id ='ZDB-EXP-070511-5')
      and xpatres_expression_found = 't'
order by mrkr_zdb_id;

{
case 4402  Weekly download file available via the web.
Fields: OMIM, ZFIN-GENE-ID, ZFIN-GENO-ID, ZIRC-ALT-ID
All lines available from ZIRC.
}

select dblink_acc_num, ortho_name, ortho_abbrev, fmrel_mrkr_zdb_id, mrkr_name, mrkr_abbrev,
	genofeat_geno_zdb_id, geno_display_name, feature_name, feature_zdb_id
  from db_link, foreign_db fdb,foreign_db_contains fdbc, orthologue, feature_marker_relationship,
  	marker, genotype_feature, int_data_supplier, feature, genotype
 where fdb.fdb_db_name = 'OMIM'
   and fdb.fdb_db_pk_id = fdbc.fdbcont_fdb_db_id
   and fdbc.fdbcont_zdb_id = dblink_fdbcont_zdb_id
   and dblink_linked_recid = zdb_id
   and c_gene_id = fmrel_mrkr_zdb_id
   and fmrel_mrkr_zdb_id = mrkr_zdb_id
   and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
   and genofeat_feature_zdb_id = feature_zdb_id
   and genofeat_feature_zdb_id = idsup_data_zdB_id
--   and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53'
   and genofeat_geno_zdb_id = geno_zdb_id
 into temp lamhdi_tmp with no log
;

insert into lamhdi_tmp(dblink_acc_num, ortho_name, ortho_abbrev, fmrel_mrkr_zdb_id, mrkr_name, mrkr_abbrev, genofeat_geno_zdb_id, geno_display_name)
select dblink_acc_num, ortho_name, ortho_abbrev, fmrel_mrkr_zdb_id, mrkr_name, mrkr_abbrev, genofeat_geno_zdb_id, geno_display_name
  from db_link, foreign_db_contains fdbc, foreign_db fdb, orthologue, feature_marker_relationship, marker, genotype_feature, int_data_supplier, genotype
 where fdb.fdb_db_name = 'OMIM'
   and fdb.fdb_db_pk_id = fdbc.fdbcont_fdb_db_id
   and fdbc.fdbcont_zdb_id = dblink_fdbcont_zdb_id
   and dblink_linked_recid = zdb_id
   and c_gene_id = fmrel_mrkr_zdb_id
   and fmrel_mrkr_zdb_id = mrkr_zdb_id
   and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
   and genofeat_geno_zdb_id = idsup_data_zdB_id
   and genofeat_geno_zdb_id = geno_zdb_id
--   and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53'
;

insert into lamhdi_tmp(dblink_acc_num, ortho_name, ortho_abbrev, fmrel_mrkr_zdb_id, mrkr_name, mrkr_abbrev, genofeat_geno_zdb_id, geno_display_name)
select distinct dblink_acc_num, ortho_name, ortho_abbrev, fmrel_mrkr_zdb_id, mrkr_name, mrkr_abbrev, genofeat_geno_zdb_id, geno_display_name
 from db_link, foreign_db_contains fdbc, foreign_db fdb, orthologue, feature_marker_relationship, marker, genotype_feature, genotype
 where fdb.fdb_db_name = 'OMIM'
   and fdb.fdb_db_pk_id = fdbc.fdbcont_fdb_db_id
   and fdbc.fdbcont_zdb_id = dblink_fdbcont_zdb_id
   and dblink_linked_recid = zdb_id
   and c_gene_id = fmrel_mrkr_zdb_id
   and fmrel_mrkr_zdb_id = mrkr_zdb_id
   and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
--   and genofeat_geno_zdb_id = idsup_data_zdB_id
   and genofeat_geno_zdb_id = geno_zdb_id
--   and idsup_supplier_zdb_id = 'ZDB-LAB-991005-53'
   and not exists (select 't' from int_data_supplier where genofeat_geno_zdb_id = idsup_data_zdB_id)
   and not exists (select 't' from int_data_supplier where genofeat_feature_zdb_id = idsup_data_zdB_id)
;

update lamhdi_tmp
set geno_display_name = geno_display_name || " (" || get_genotype_backgrounds(genofeat_geno_zdb_id) || ")"
where exists (select 't' from genotype_background where genoback_geno_zdb_id = genofeat_geno_zdb_id);

! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/lamhdi.unl'"
unload to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/lamhdi.unl'
select distinct dblink_acc_num, ortho_name, ortho_abbrev, fmrel_mrkr_zdb_id, mrkr_name,
	mrkr_abbrev, genofeat_geno_zdb_id, geno_display_name, feature_name, feature_zdb_id
 from lamhdi_tmp
;

drop table lamhdi_tmp;

-- download file Case 4200 as reuqested by uniprot

select recattrib_data_zdb_id geneid, count(recattrib_source_zdb_id) pubcount
 from record_attribution
 where recattrib_data_zdb_id[1,8] = 'ZDB-GENE'
 group by recattrib_data_zdb_id
 into temp tmp_gene_pubcount with no log
;

create index  tmp_gene_pubcount_pubcount_idx on tmp_gene_pubcount(pubcount) in idxdbs3;

update statistics medium for table tmp_gene_pubcount;

-- why isn't this file tab delimited?
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/uniprot-zfinpub.txt'"
unload to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/uniprot-zfinpub.txt'
select geneid, dblink_acc_num,zdb_id,accession_no,'Expression' as  cur_topic
 from db_link, foreign_db_contains fdbc, foreign_db fdb, publication,
 	tmp_gene_pubcount, expression_experiment
 where geneid=dblink_linked_recid
   and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
   and fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
   and fdb.fdb_db_name = 'UniProtKB'
   and geneid=xpatex_gene_zdb_id
   and xpatex_source_zdb_id=zdb_id
   and pubcount <= 20
   and jtype='Journal'
union
select geneid, dblink_acc_num,zdb_id,accession_no,'GO' as  cur_topic
 from db_link, foreign_db_contains fdbc, foreign_db fdb, publication,
 	tmp_gene_pubcount, marker_go_term_evidence
 where geneid=dblink_linked_recid
   and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
   and fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
   and fdb.fdb_db_name = 'UniProtKB'
   and geneid=mrkrgoev_mrkr_zdb_id
   and mrkrgoev_source_zdb_id=zdb_id
   and pubcount <= 20
   and jtype='Journal'
union
select geneid, dblink_acc_num,zdb_id,accession_no,'Phenotype' as  cur_topic
from db_link, foreign_db_contains fdbc, foreign_db fdb,  publication,gene_pubcount, feature_marker_relationship, genotype_feature, genotype_experiment, experiment, phenotype_experiment, phenotype_statement, figure
where geneid=dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and geneid = fmrel_mrkr_zdb_id
and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
and genofeat_geno_zdb_id = genox_geno_zdb_id
and genox_zdb_id = phenox_genox_zdb_id
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount <= 20
and jtype='Journal'
and (exp_name ='_Standard' or exp_name like '%Generic%')
and exp_zdb_id=genox_exp_zdb_id
and phenos_tag!='normal'
union
select geneid,dblink_acc_num,zdb_id,accession_no,'Phenotype' as cur_topic 
from phenotype_experiment, phenotype_statement, figure, gene_pubcount,foreign_db_contains fdbc, foreign_db fdb, db_link, publication, mutant_fast_search
where phenox_genox_zdb_id = mfs_genox_zdb_id
and geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and mfs_mrkr_zdb_id = geneid
and mfs_mrkr_zdb_id like 'ZDB-GENE%'
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount <= 20
and jtype='Journal'
and phenos_tag!='normal'
>>>>>>> .merge-right.r23287
union
select geneid,dblink_acc_num,zdb_id,accession_no,'Phenotype' as cur_topic 
from phenotype_experiment, phenotype_statement, figure, gene_pubcount, foreign_db_contains fdbc, foreign_db fdb, db_link, publication, mutant_fast_search, marker_relationship
where phenox_genox_zdb_id = mfs_genox_zdb_id
and geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and mfs_mrkr_zdb_id = mrel_mrkr_1_zdb_id
and mfs_mrkr_zdb_id like 'ZDB-MRPHLNO%'
and mrel_mrkr_2_zdb_id = geneid
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount <= 20
and jtype = 'Journal'
and phenos_tag != 'normal'
union
select geneid, dblink_acc_num,zdb_id,accession_no,'GO' as  cur_topic
from db_link, foreign_db_contains fdbc, foreign_db fdb,  publication,gene_pubcount, marker_go_term_evidence
where geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and geneid = mrkrgoev_mrkr_zdb_id
and mrkrgoev_source_zdb_id = zdb_id
and pubcount > 20
and jtype = 'Journal'
union
select geneid, dblink_acc_num,zdb_id,accession_no,'Phenotype' as  cur_topic
from db_link, foreign_db_contains fdbc, foreign_db fdb,  publication, gene_pubcount, feature_marker_relationship, genotype_feature, genotype_experiment, experiment, phenotype_experiment, phenotype_statement, figure
where geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and geneid = fmrel_mrkr_zdb_id
and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
and genofeat_geno_zdb_id = genox_geno_zdb_id
and genox_zdb_id = phenox_genox_zdb_id
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount > 20
and jtype='Journal'
and (exp_name ='_Standard' or exp_name like '%Generic%')
and exp_zdb_id=genox_exp_zdb_id
and phenos_tag!='normal'
union
select geneid,dblink_acc_num,zdb_id,accession_no,'Phenotype' as cur_topic 
from phenotype_experiment, phenotype_statement, figure, gene_pubcount, foreign_db_contains fdbc, foreign_db fdb, db_link, publication, mutant_fast_search
where phenox_genox_zdb_id = mfs_genox_zdb_id
and geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and mfs_mrkr_zdb_id = geneid
and mfs_mrkr_zdb_id like 'ZDB-GENE%'
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount > 20
and jtype='Journal'
and phenos_tag !='normal'
union
select geneid,dblink_acc_num,zdb_id,accession_no,'Phenotype' as cur_topic 
from phenotype_experiment, phenotype_statement, figure, gene_pubcount,foreign_db_contains fdbc, foreign_db fdb, db_link, publication,mutant_fast_search, marker_relationship
where phenox_genox_zdb_id = mfs_genox_zdb_id
and geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and mfs_mrkr_zdb_id = mrel_mrkr_1_zdb_id
and mfs_mrkr_zdb_id like 'ZDB-MRPHLNO%'
and mrel_mrkr_2_zdb_id = geneid
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount > 20
and phenos_tag !='normal'
and jtype='Journal';

-- download file Case 4693 as reuqested by uniprot
! echo "'<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfinpubs.txt'"
UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfinpubs.txt'
 DELIMITER "	"
select zdb_id, accession_no, authors,title,jrnl_name,year(pub_date),pub_volume,pub_pages
 from publication, journal
 where pub_jrnl_zdb_id = jrnl_zdb_id
   and jtype = 'Journal'
;
