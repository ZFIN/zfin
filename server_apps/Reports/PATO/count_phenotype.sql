begin work ;

-------------------non-transgenics-------------------------------

-- ct1
\echo number of genes with phenotypes, non-transgenic only ;

select count(distinct fmrel_mrkr_zdb_id)
 from feature_marker_relationship, feature, genotype_feature,
      fish, fish_experiment,
      phenotype_experiment        
 where fmrel_type = 'is allele of'   
 and fmrel_ftr_zdb_id = feature_zdb_id
 and feature_type != 'TRANSGENIC_INSERTION' 
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and fish_Zdb_id = genox_fish_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id;

-- ct2
\echo number of extra genes found via morpholinos with phenotypes, non-transgenic only, in mutant genotypes only ;

select count(distinct mrel_mrkr_2_zdb_id)
   from marker_relationship,
        fish_str, fish,
        fish_experiment,
        phenotype_experiment,
        genotype_feature,
        genotype,
        feature_marker_relationship
   where mrel_type = 'knockdown reagent targets gene'
   and mrel_mrkr_1_zdb_id like 'ZDB-MRPHLNO%'
   and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id 
   and fishstr_fish_zdb_id = fish_Zdb_id
   and fish_zdb_id = genox_fish_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and fish_genotype_Zdb_id = genofeat_geno_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
   and geno_is_wildtype = 'f'
   and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
   and mrel_mrkr_1_zdb_id != fmrel_mrkr_zdb_id;

-- ct3   
\echo number of genes whose morpholinos are used in fish with WT genotypes that have phenotypes;

--transgenic doesn't matter because we're on a WT background

select count(distinct mrel_mrkr_2_zdb_id)
   from marker_relationship,
        fish_str, fish,
        genotype, fish_experiment,
        phenotype_experiment
   where mrel_type = 'knockdown reagent targets gene'
   and mrel_mrkr_1_zdb_id like 'ZDB-MRPHLNO%' 
   and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
   and fishstr_fish_zdb_id = fish_zdb_id
   and geno_zdb_id = fish_genotype_Zdb_id
   and geno_is_wildtype = 't'
   and fish_zdb_id = genox_fish_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id;

-- ct4
\echo number of genes with phenotypes and images: not transgenic ;

select count(distinct fmrel_mrkr_zdb_id)
 from feature_marker_relationship,
      feature,
      genotype_feature, 
      fish,
      fish_experiment,
      phenotype_experiment,
      figure,
      image      
 where fmrel_type = 'is allele of'
 and fmrel_ftr_zdb_id = feature_zdb_id 
 and feature_type != 'TRANSGENIC_INSERTION'
 and fmrel_ftr_zdb_id = genofeat_feature_zdb_id 
 and fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and genox_fish_zdb_id = fish_Zdb_id  
 and phenox_genox_zdb_id = genox_zdb_id 
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null;
 
-- ct5
\echo number of genes whose morpholinos are used in fish with non-transgenic, non-WT genotypes that have phenotypes and images ;

select count(distinct mrel_mrkr_2_zdb_id)
   from marker_relationship,
        fish_str, fish,
        genotype_feature, feature, genotype,
        fish_experiment,
        phenotype_experiment,
        figure,
        image
   where mrel_type = 'knockdown reagent targets gene'
   and mrel_mrkr_1_zdb_id like 'ZDB-MRPHLNO%'
   and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
   and fishstr_fish_Zdb_id = fish_zdb_id
   and fish_genotype_zdb_id = genofeat_geno_zdb_id
   and genofeat_feature_zdb_id = feature_zdb_id
   and feature_type != 'TRANSGENIC_INSERTION'
   and geno_zdb_id = genofeat_geno_zdb_id
   and geno_is_wildtype = 'f'
   and genox_fish_Zdb_id = fish_Zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and phenox_fig_zdb_id = fig_zdb_id
   and img_fig_zdb_id = fig_zdb_id
   and img_image is not null;

---------------------------------features----------------------------
-- ct6
\echo number of features with phenotypes; non-transgenic features only ;

select count(distinct fmrel_ftr_zdb_id)
 from feature_marker_relationship, feature, genotype_feature,
      fish, fish_experiment, phenotype_experiment
 where fmrel_type = 'is allele of'
 and fmrel_ftr_zdb_id = feature_zdb_id
 and feature_type != 'TRANSGENIC_INSERTION'
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and genox_fish_zdb_id = fish_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and not exists (select 'x' from fish_str where fishstr_fish_Zdb_id = fish_zdb_id); 

-- ct7
\echo number of features with phenotypes and images; non-transgenic features only ;

select count(distinct fmrel_ftr_zdb_id)
 from feature_marker_relationship, feature, genotype_feature, fish, fish_experiment, phenotype_experiment, 
      figure, image
 where fmrel_type = 'is allele of'
 and fmrel_ftr_zdb_id = feature_zdb_id
 and feature_type != 'TRANSGENIC_INSERTION'
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and genox_fish_zdb_id = fish_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null 
 and not exists (select 'x' from fish_str where fishstr_fish_Zdb_id = fish_zdb_id);

---------------------------- transgenics------------------------------------------
-- ct8
\echo number of transgenic constructs with phenotypes (Transgenic constructs with Phenotypes) ;

select count(distinct fmrel_mrkr_zdb_id)
 from feature_marker_relationship, feature, genotype_feature,
      fish, fish_experiment, phenotype_experiment
 where fmrel_type = 'contains phenotypic sequence feature'
 and fmrel_ftr_zdb_id = feature_zdb_id
 and feature_type = 'TRANSGENIC_INSERTION' 
 and get_obj_type(fmrel_mrkr_zdb_id) in ('TGCONSTRCT', 'ETCONSTRCT', 'GTCONSTRCT', 'PTCONSTRCT')
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and genofeat_geno_zdb_id = fish_genotype_Zdb_id
 and fish_Zdb_id = genox_Fish_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id;

-- ct9
\echo number of transgenic constructs with phenotypes and images ;

select count(distinct fmrel_mrkr_zdb_id)
 from feature_marker_relationship, feature, genotype_feature, fish,
      fish_experiment, phenotype_experiment,
      figure, image
 where fmrel_type in ('contains sequence feature', 'contains phenotypic sequence feature')
 and fmrel_ftr_zdb_id = feature_zdb_id
 and feature_type = 'TRANSGENIC_INSERTION'
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and fish_genotype_zdb_id = genofeat_geno_zdb_id
 and genox_fish_Zdb_id = fish_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null;

-- ct10
\echo number of distinct genes whose morpholinos are used in fish where the fish have tg insertion features and produce phenotypes ;

select count(distinct mrel_mrkr_2_zdb_id)
   from marker_relationship, fish_str, fish,
        genotype_feature, feature, genotype,
        fish_experiment, phenotype_experiment
   where mrel_type = 'knockdown reagent targets gene'
   and mrel_mrkr_1_zdb_id like 'ZDB-MRPHLNO%'
   and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
   and fishstr_fish_zdb_id = fish_Zdb_id
   and fish_genotype_Zdb_id = genofeat_geno_zdb_id
   and genofeat_feature_zdb_id = feature_zdb_id
   and feature_type = 'TRANSGENIC_INSERTION'
   and geno_zdb_id = genofeat_geno_zdb_id
   and geno_is_wildtype = 'f'
   and fish_zdb_id = genox_fish_zdb_id
   and phenox_genox_zdb_id = genox_Zdb_id;

-- ct11
\echo number of distinct genes whose morpholinos are used in fish, where the fish have tg insertion features and produce phenotypes and have images ;

select count(distinct mrel_mrkr_2_zdb_id)
   from marker_relationship, fish_str,
        fish_experiment,fish,
        phenotype_experiment,
        figure, image,
        genotype_feature, feature
   where mrel_type = 'knockdown reagent targets gene'
   and mrel_mrkr_1_zdb_id like 'ZDB-MRPHLNO%'
   and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
   and fishstr_fish_zdb_id = fish_zdb_id
   and fish_zdb_id = genox_fish_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and phenox_fig_zdb_id = fig_zdb_id
   and img_fig_zdb_id = fig_zdb_id
   and img_image is not null
   and fish_genotype_Zdb_id = genofeat_geno_zdb_id
   and genofeat_feature_zdb_id = feature_zdb_id
   and feature_type = 'TRANSGENIC_INSERTION';

-- ct12
\echo number of transgenic insertion features with phenotypes ;

select count(distinct genofeat_feature_zdb_id)
 from genotype_feature, feature, fish, fish_experiment, phenotype_experiment
 where genofeat_feature_zdb_id = feature_zdb_id
 and feature_type = 'TRANSGENIC_INSERTION'
 and fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and fish_zdb_id = genox_fish_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id;
 
-- ct13 
\echo number of transgenic insertion features with phenotypes and images ;

select count(distinct genofeat_feature_zdb_id)
 from genotype_feature, feature, fish, fish_experiment,
      phenotype_experiment, figure, image
 where genofeat_feature_zdb_id = feature_zdb_id
 and feature_type = 'TRANSGENIC_INSERTION'
 and fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and fish_zdb_id = genox_fish_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null;

-- ct14
\echo transgenic fish with number of distinct constructs that have at least 1 image (either FX or pheno) ;

create temp table tmp_distinct_genes_with_images_trans as
select distinct fmrel_mrkr_zdb_id
 from feature_marker_relationship, feature, genotype_feature, fish,
      fish_experiment, phenotype_experiment, figure, image
 where fmrel_type = 'contains sequence feature'
 and fmrel_ftr_zdb_id = feature_zdb_id
 and feature_type = 'TRANSGENIC_INSERTION' 
 and get_obj_type(fmrel_mrkr_zdb_id) in ('TGCONSTRCT', 'ETCONSTRCT', 'GTCONSTRCT', 'PTCONSTRCT')
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and genox_fish_zdb_id = fish_Zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
union
  select distinct fmrel_mrkr_zdb_id
   from feature_marker_relationship, feature, genotype_feature,
        fish, fish_experiment, expression_experiment2,
        expression_result2, expression_figure_stage,
        figure, image
        where feature_zdb_id = fmrel_ftr_zdb_id
        and feature_type = 'TRANSGENIC_INSERTION'
        and feature_zdb_id = genofeat_feature_zdb_id
        and fish_genotype_zdb_id = genofeat_geno_zdb_id
        and genox_fish_zdb_id = fish_Zdb_id
        and xpatex_genox_zdb_id = genox_zdb_id
        and xpatex_zdb_id = efs_xpatex_zdb_id
        and xpatres_efs_id = efs_pk_id
        and efs_fig_zdb_id = fig_Zdb_id
        and fig_zdb_id = img_fig_zdb_id
        and img_image is not null;

select count(*) from tmp_distinct_genes_with_images_trans;

-------------------------PAPERS---------------------------------
-- ct15
\echo Number of direct submission phenotype papers ;

select count(distinct fig_source_zdb_id)
    from figure, phenotype_source_generated, publication
    where fig_zdb_id = pg_fig_zdb_id
      and fig_source_zdb_id = zdb_id
      and jtype in ('Curation', 'Active Curation', 'Unpublished');
        
-- ct16        
\echo Number of direct submission phenotype records ;

select count(distinct pg_id)
  from phenotype_observation_generated, phenotype_source_generated, figure, publication
    where psg_pg_id = pg_id
      and fig_zdb_id = pg_fig_zdb_id
      and fig_source_zdb_id = zdb_id
      and jtype in ('Curation', 'Active Curation', 'Unpublished');

-- ct17
\echo number of non-curation, published papers with phenotypes;

select count(distinct fig_source_zdb_id)
  from figure, phenotype_source_generated, publication
    where pg_fig_zdb_id  = fig_zdb_id
      and fig_source_zdb_id = zdb_id
      and jtype != 'Curation'      
      and jtype != 'Unpublished';
        
-- ct18        
\echo number of phenotypes (EQs) total;

select count(psg_id)
  from phenotype_observation_generated;

------------------IMAGES xpat and PATO or just xpat-------------------------

-- ct19
\echo number of genes with expression images ;

select count(distinct xpatex_gene_zdb_id)
	from expression_experiment2,
	expression_result2,
	expression_figure_stage, figure, image
	where xpatex_zdb_id = efs_xpatex_zdb_id
	and xpatres_efs_id = efs_pk_id
	and efs_fig_zdb_id = fig_Zdb_id
	and fig_zdb_id = img_fig_zdb_id
	and img_image is not null;

-- ct20
\echo total number of distinct genes with images (either FX or PATO images).  genes included: those in non-transgenic genotypes with phenotype images, those in non-transgeinc genotype-backgrounds with FX images, those in FX experiments ;

create temp table tmp_distinct_genes_with_images as
select distinct fmrel_mrkr_zdb_id
 from feature_marker_relationship, feature, genotype_feature, fish,
      phenotype_experiment, fish_experiment, figure, image
 where fmrel_type = 'is allele of'
 and fmrel_ftr_zdb_id = feature_zdb_id
 and feature_type != 'TRANSGENIC_INSERTION'
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and genox_fish_zdb_id = fish_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
union
  select distinct xpatex_gene_zdb_id
      from expression_experiment2, expression_result2, expression_figure_stage, figure, image,
         fish_experiment,fish, genotype_feature, feature
      where xpatex_zdb_id = efs_xpatex_zdb_id
        and xpatres_efs_id = efs_pk_id
        and efs_fig_zdb_id = fig_Zdb_id
        and fig_zdb_id = img_fig_zdb_id
        and img_image is not null
        and xpatex_genox_zdb_id = genox_zdb_id
        and genox_fish_zdb_id = fish_Zdb_id
        and fish_genotype_Zdb_id = genofeat_geno_zdb_id
        and feature_zdb_id = genofeat_feature_zdb_id
        and feature_type != 'TRANSGENIC_INSERTION'
union
  select distinct xpatex_gene_zdb_id
    from expression_experiment2, expression_result2, expression_figure_stage, figure, image
      where xpatex_zdb_id = efs_xpatex_zdb_id
        and xpatres_efs_id = efs_pk_id
        and efs_fig_zdb_id = fig_Zdb_id
        and fig_zdb_id = img_fig_zdb_id
        and img_image is not null;

select count(*) from tmp_distinct_genes_with_images;

commit work ;

--rollback work ;


