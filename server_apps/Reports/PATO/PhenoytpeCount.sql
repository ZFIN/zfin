begin work ;

-------------------non-transgenics-------------------------------

!echo "number of genes with phenotypes, non-transgenic only" ;

create temp table tmp_genes_with_phenos (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos
select distinct fmrel_mrkr_zdb_id
 from genotype, 
	phenotype_experiment,
	genotype_experiment,
	genotype_feature, 
	feature_marker_relationship
 where genox_geno_Zdb_id = genofeat_geno_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_geno_zdb_id = geno_Zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and fmrel_type = 'is allele of' 
 and get_feature_type(fmrel_ftr_zdb_id) != 'TRANSGENIC_INSERTION';

select count(*) from tmp_genes_with_phenos ;

create temp table tmp_genes_with_phenos_m (mrkr_id varchar(50))
with no log ;

!echo "number of extra genes found via morpholinos with phenotypes, non-transgenic only, in mutant genotypes only" ;

insert into tmp_genes_with_phenos_m
 select distinct mrel_mrkr_2_zdb_id
   from marker_relationship, 
	experiment_condition,
        genotype_experiment, 
	genotype_feature,
	genotype,
        phenotype_experiment,
	feature_marker_relationship
   where mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id
   and expcond_exp_zdb_id = genox_exp_zdb_id
   and genox_geno_Zdb_id = genofeat_geno_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and mrel_mrkr_1_zdb_id != fmrel_mrkr_zdb_id
   and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
   and geno_is_wildtype = 'f';

select count(*) from tmp_genes_with_phenos_m;

!echo "number of genes whose morpholinos are using in environments on WT genotypes that have phenotypes";

--transgenic doesn't matter because we're on a WT background
create temp table tmp_genes_with_phenos_m_w (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos_m_w
 select distinct mrel_mrkr_2_zdb_id
   from marker_relationship,
	experiment_condition,
        genotype_experiment,
	genotype,
        phenotype_experiment
   where mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id
   and expcond_exp_zdb_id = genox_exp_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and geno_is_wildtype = 't'
   and geno_zdb_id = genox_geno_Zdb_id;

select count(*) from tmp_genes_with_phenos_m_w;

!echo "number of genes with phenotypes and images: not transgenic" ;

create temp table tmp_genes_with_phenos_imgs (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos_imgs 
select distinct fmrel_mrkr_zdb_id
 from genotype, 
	phenotype_experiment,
	genotype_experiment,
	genotype_feature, 
	feature_marker_relationship,
	image,
	figure
 where genox_geno_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_geno_zdb_id = geno_Zdb_id
 and fmrel_type = 'is allele of' 
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
  and get_feature_type(fmrel_ftr_zdb_id) != 'TRANSGENIC_INSERTION';

select count(*) from tmp_genes_with_phenos_imgs ;

!echo "number of genes whose morpholinos are used in environments with non-transgenic, non-WT genotypes that have phenotypes and images" ;

create temp table tmp_genes_with_phenos_imgs_m (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos_imgs_m
 select distinct mrel_mrkr_2_zdb_id
   from marker_relationship, 
	experiment_condition,
        genotype_experiment, 
	genotype_feature,
        phenotype_experiment,
	figure, 
	image, genotype
   where mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id
   and expcond_exp_zdb_id = genox_exp_zdb_id
   and genox_geno_Zdb_id = genofeat_geno_zdb_id
   and genox_geno_zdb_id = geno_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and phenox_fig_zdb_id = fig_zdb_id
   and img_fig_zdb_id = fig_zdb_id
   and img_image is not null
 and get_feature_type(genofeat_feature_zdb_id) != 'TRANSGENIC_INSERTION'
 and geno_is_wildtype = 'f';

select count(*) from tmp_genes_with_phenos_imgs_m;

---------------------------------features----------------------------

!echo "number of features with phenotypes; non-transgenic features only" ;

create temp table tmp_genes_with_phenos_non_tg (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos_non_tg
select distinct fmrel_ftr_zdb_id
 from genotype, phenotype_experiment, genotype_experiment,
	genotype_feature, feature_marker_relationship
 where genox_geno_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_geno_zdb_id = geno_Zdb_id
 and fmrel_type = 'is allele of' 
 and get_feature_type(fmrel_ftr_zdb_id) != 'TRANSGENIC_INSERTION';

select count(*) from tmp_genes_with_phenos_non_tg;

!echo "number of features with phenotypes and images; non-transgenic features only" ;

create temp table tmp_genes_with_phenos_imgs_non_tgs (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos_imgs_non_tgs
select distinct fmrel_ftr_zdb_id
 from genotype, phenotype_experiment, genotype_experiment,
	genotype_feature, feature_marker_relationship,
	image, figure
 where genox_geno_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_geno_zdb_id = geno_Zdb_id
 and fmrel_type = 'is allele of' 
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
 and get_feature_type(fmrel_ftr_zdb_id) != 'TRANSGENIC_INSERTION';

select count(*) from tmp_genes_with_phenos_imgs_non_tgs ;


---------------------------- transgenics------------------------------------------

!echo "number of transgenic constructs with phenotypes (Transgenic constructs w/Phenotypes)" ;

create temp table tmp_tg_genes_with_phenos (mrkr_id varchar(50))
with no log ;

insert into tmp_tg_genes_with_phenos
select distinct fmrel_mrkr_zdb_id
 from genotype, 
	phenotype_experiment,
	genotype_experiment,
        genotype_feature, 
	feature_marker_relationship, 
	feature
 where genox_geno_Zdb_id = geno_zdb_id
and genofeat_geno_zdb_id = geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
and genofeat_feature_zdb_id = feature_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
and fmrel_type in ('contains phenotypic sequence feature')
 and get_feature_type(fmrel_ftr_zdb_id) = 'TRANSGENIC_INSERTION'
 and get_obj_type(fmrel_mrkr_zdb_id) = 'TGCONSTRCT'
and feature_zdb_id = genofeat_feature_zdb_id;

select count(*) from tmp_tg_genes_with_phenos;

!echo "number of transgenic constructs with phenotypes and images" ;

create temp table tmp_genes_with_phenos_imgs_tgs (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos_imgs_tgs
select distinct fmrel_mrkr_zdb_id
 from genotype, phenotype_experiment, genotype_experiment,
	genotype_feature, feature_marker_relationship,
	image, figure
 where genox_geno_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_geno_zdb_id = geno_Zdb_id
 and genofeat_geno_zdb_id = geno_zdb_id
 and fmrel_type in ('contains sequence feature', 'contains phenotypic sequence feature') 
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
  and get_feature_type(fmrel_ftr_zdb_id) = 'TRANSGENIC_INSERTION';

select count(*) from tmp_genes_with_phenos_imgs_tgs;

!echo "number of distinct genes whose morpholinos are being used in genotype environments, where the genotypes have tg insertion features and produce phenotypes" ;

create temp table tmp_genes_with_phenos_m_tg (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos_m_tg
 select distinct mrel_mrkr_2_zdb_id
   from marker_relationship, 
	experiment_condition,
        genotype_experiment, 
	genotype_feature,
	genotype,
        phenotype_experiment
   where mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id
   and expcond_exp_zdb_id = genox_exp_zdb_id
   and genox_geno_Zdb_id = genofeat_geno_zdb_id
   and geno_zdb_id =genox_geno_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
   and get_feature_type(genofeat_feature_zdb_id) = 'TRANSGENIC_INSERTION'
   and geno_zdb_id = genofeat_geno_zdb_id
   and geno_is_wildtype = 'f'
   and phenox_genox_zdb_id = genox_Zdb_id;

select count(*) from tmp_genes_with_phenos_m_tg;

!echo "number of distinct genes whose morpholinos are being used in genotype environments, where the genotypes have tg insertion features and produce phenotypes and have images" ;

create temp table tmp_genes_with_phenos_imgs_tgs_m (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos_imgs_tgs_m
 select distinct mrel_mrkr_2_zdb_id
   from marker_relationship, 
	experiment_condition,
        genotype_experiment, 
	genotype_feature,
        phenotype_experiment,
	figure, 
	image
   where mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id
   and expcond_exp_zdb_id = genox_exp_zdb_id
   and genox_geno_Zdb_id = genofeat_geno_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and phenox_fig_zdb_id = fig_zdb_id
   and img_fig_zdb_id = fig_zdb_id
   and img_image is not null
 and get_feature_type(genofeat_feature_zdb_id) = 'TRANSGENIC_INSERTION';

select count(*) from tmp_genes_with_phenos_imgs_tgs_m ;

!echo "number of transgenic insertion features with phenotypes" ;

create temp table tmp_genes_with_phenos_tg (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos_tg
select distinct genofeat_feature_zdb_id
 from genotype, phenotype_experiment, genotype_experiment,
	genotype_feature
 where genox_geno_Zdb_id = genofeat_geno_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_geno_zdb_id = geno_Zdb_id
 and get_feature_type(genofeat_feature_zdb_id) = 'TRANSGENIC_INSERTION';

select count(*) from tmp_genes_with_phenos_tg ;

!echo "number of transgenic insertion features with phenotypes and images" ;

create temp table tmp_genes_with_phenos_imgs_trans (mrkr_id varchar(50))
with no log ;

insert into tmp_genes_with_phenos_tg
select distinct genofeat_feature_zdb_id
 from genotype, phenotype_experiment, genotype_experiment,
	genotype_feature, image, figure
 where genox_geno_Zdb_id = genofeat_geno_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_geno_zdb_id = geno_Zdb_id
 and get_feature_type(genofeat_feature_zdb_id) = 'TRANSGENIC_INSERTION'
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null;

select count(*) from tmp_genes_with_phenos_imgs_trans ;

!echo "total number of distinct transgenic constructs with images (both tgconstructs with either FX images and/or tg constructs with PATO images)" ;

select distinct fmrel_mrkr_zdb_id as gene_id
 from genotype, 
	phenotype_experiment,
	genotype_experiment,
	genotype_feature, 
	feature_marker_relationship,
	image,
	figure
 where genox_geno_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_geno_zdb_id = geno_Zdb_id
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
 and fmrel_type = 'contains sequence feature'
 and get_feature_type(fmrel_ftr_zdb_id) = 'TRANSGENIC_INSERTION'
and get_obj_type(fmrel_mrkr_zdb_id) = 'TGCONSTRCT'
union
  select distinct fmrel_mrkr_zdb_id
	from expression_Experiment,
	feature_marker_relationship,
	expression_result,
	genotype, 
	genotype_experiment,
	expression_pattern_figure, 
	figure, 
	image, 
	genotype_feature, 
	feature
	where xpatex_zdb_id = xpatres_xpatex_zdb_id
	and xpatres_zdb_id = xpatfig_xpatres_zdb_id
	and xpatfig_fig_zdb_id = fig_Zdb_id
	and fig_zdb_id = img_fig_zdb_id
	and img_image is not null
        and xpatex_genox_zdb_id = genox_zdb_id
        and genox_geno_zdb_id = geno_Zdb_id
	and geno_zdb_id = genofeat_geno_zdb_id
	and feature_zdb_id = genofeat_feature_zdb_id
        and feature_type = 'TRANSGENIC_INSERTION' 
	and feature_zdb_id = fmrel_ftr_zdb_id
        and genofeat_feature_zdb_id = fmrel_ftr_Zdb_id
        and get_feature_type(fmrel_mrkr_zdb_id)='TRANSGENIC_CONSTRUCT'
into temp tmp_distinct_genes_with_images_trans;

select count(*) from tmp_distinct_genes_with_images_trans;

-------------------------PAPERS---------------------------------
!echo "Number of direct submission phenotype papers" ;

create temp table tmp_papers_with_phenotype_ds (paper_id varchar(50))
with no log ;

insert into tmp_papers_with_phenotype_ds
  select distinct fig_source_zdb_id
    from phenotype_experiment, publication, figure
    where fig_source_zdb_id = zdb_id
        and fig_zdb_id = phenox_fig_zdb_id
        and jtype = 'Curation';

select count(*) from tmp_papers_with_phenotype_ds;

!echo "Number of direct submission phenotype records" ;

create temp table tmp_papers_with_phenotype_ds_r (paper_id varchar(50))
with no log ;

insert into tmp_papers_with_phenotype_ds_r
select distinct phenos_pk_id
    from phenotype_statement, publication, figure, phenotype_experiment
    where fig_source_zdb_id = zdb_id
        and fig_zdb_id = phenox_fig_zdb_id
        and phenox_pk_id = phenos_phenox_pk_id
        and jtype = 'Curation';

select count(*) from tmp_papers_with_phenotype_ds_r;

!echo "number of non-curation, published papers with phenotypes";

create temp table tmp_papers_with_phenotype (paper_id varchar(50))
with no log ;

insert into tmp_papers_with_phenotype
  select distinct fig_source_zdb_id
    from phenotype_experiment, publication, figure
    where fig_source_zdb_id = zdb_id
        and phenox_fig_zdb_id  = fig_zdb_id
	    and jtype != 'Curation'
        and jtype != 'Unpublished';

select count(*) from tmp_papers_with_phenotype;

!echo "number of phenotypes (EQs) total";

create temp table tmp_phenotype (paper_id varchar(50))
with no log ;

insert into tmp_phenotype
  select distinct phenos_pk_id
    from phenotype_statement;

select count(*) from tmp_phenotype;


------------------IMAGES xpat and PATO or just xpat-------------------------

!echo "number of genes with expression images" ;

create temp table tmp_genes_with_expression_images (gene_id varchar(50))
with no log ;

insert into tmp_genes_with_expression_images
  select distinct xpatex_gene_zdb_id
	from expression_Experiment,
	expression_result,
	expression_pattern_figure, figure, image
	where xpatex_zdb_id = xpatres_xpatex_zdb_id
	and xpatres_zdb_id = xpatfig_xpatres_zdb_id
	and xpatfig_fig_zdb_id = fig_Zdb_id
	and fig_zdb_id = img_fig_zdb_id
	and img_image is not null;

select count(*) from tmp_genes_with_expression_images ;

!echo "total number of distinct genes with images (either FX or PATO images).  genes included: those in non-transgenic genotypes with phenotype images, those in non-transgeinc genotype-backgrounds with FX images, those in FX experiments" ;

select distinct fmrel_mrkr_zdb_id as gene_id
 from genotype, phenotype_experiment, genotype_experiment,
	genotype_feature, feature_marker_relationship,
	image, figure
 where genox_geno_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_geno_zdb_id = geno_Zdb_id
 and fmrel_type = 'is allele of' 
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
 and get_feature_type(fmrel_ftr_zdb_id) != 'TRANSGENIC_INSERTION'
union
  select distinct xpatex_gene_zdb_id
	from expression_Experiment,
	expression_result,genotype, genotype_experiment,
	expression_pattern_figure, figure, image, genotype_feature, feature
	where xpatex_zdb_id = xpatres_xpatex_zdb_id
	and xpatres_zdb_id = xpatfig_xpatres_zdb_id
	and xpatfig_fig_zdb_id = fig_Zdb_id
	and fig_zdb_id = img_fig_zdb_id
	and img_image is not null
        and xpatex_genox_zdb_id = genox_zdb_id
        and genox_geno_zdb_id = geno_Zdb_id
	and geno_zdb_id = genofeat_geno_zdb_id
	and feature_zdb_id = genofeat_feature_zdb_id
        and feature_type != 'TRANSGENIC_INSERTION'
union
  select distinct xpatex_gene_zdb_id
		from expression_Experiment,
	expression_result,expression_pattern_figure, figure, image
	where xpatex_zdb_id = xpatres_xpatex_zdb_id
	and xpatres_zdb_id = xpatfig_xpatres_zdb_id
	and xpatfig_fig_zdb_id = fig_Zdb_id
	and fig_zdb_id = img_fig_zdb_id
	and img_image is not null 
into temp tmp_distinct_genes_with_images;

select count(*) from tmp_distinct_genes_with_images;

commit work ;

--rollback work ;

