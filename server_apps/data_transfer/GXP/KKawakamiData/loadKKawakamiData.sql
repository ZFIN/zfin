-- loadKKawakamiData.sql
-- One-time use script for FB case 5103 "Data to load for K Kawakami"
-- Input file was provided by Ceri and pre-processed by Xiang
-- This script is kept for being adapted to future similar requests
-- Add 72 feature/genotype data and 102 expression data
-- input: KKawakami.unl
-- output: a bunch of tables at ZFIN get new data inserted
-- The hard-coded data associated with this loading:
--   ZDB-FIG-080326-85  (Fig. 1 of ZDB-PUB-071219-1)
--   ZDB-EXP-070511-5   (generic control)
--   Intrinsic fluorescence
--   ZDB-EFG-070117-1   (eGFP)   
--   ZDB-PUB-071219-1   (Nagayoshi et al. 2008)

begin work;

create temp table KKawakami (
        kk_feature_name varchar(255) not null,
        kk_construct varchar(50) not null,
        kk_feature_abbrev varchar(70) not null,
        kk_feature_type varchar(30) not null,
        kk_data_source varchar(50) not null,
        kk_mutagee varchar(20) not null,
        kk_mutagen varchar(20) not null,
        kk_note1 varchar(30) not null,
        kk_note2 varchar(30) not null,
        kk_gene_expressed varchar(50) not null,
        kk_ao varchar(50) not null, 
        kk_stage varchar(50) not null
) with no log;


load from KKawakami.unl
 insert into KKawakami;

create table pre_feature (
        preftr_feature_name varchar(255),
        preftr_construct varchar(50),
        preftr_feature_abbrev varchar(70),
        preftr_feature_type varchar(30),
        preftr_data_source varchar(50),
        preftr_mutagee varchar(20),
        preftr_mutagen varchar(20),
        preftr_source varchar(50),
        preftr_notes varchar(60)
);

insert into pre_feature (
      preftr_feature_name,
      preftr_construct,
      preftr_feature_abbrev,
      preftr_feature_type,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_source,
      preftr_notes)
  select distinct kk_feature_name,
                  kk_construct,
                  kk_feature_abbrev,
                  kk_feature_type,
                  kk_data_source,
                  kk_mutagee,
                  kk_mutagen,
                  kk_data_source,
                  kk_note1 || '; ' || kk_note2
    from KKawakami;
    
alter table pre_feature add preftr_feature_zdb_id varchar(50);

update pre_feature set preftr_feature_zdb_id = get_id('ALT');


insert into zdb_active_data select preftr_feature_zdb_id from pre_feature;
! echo "         into zdb_active_data table."

-- load feature table
insert into feature (
    feature_zdb_id,
    feature_name,
    feature_abbrev,
    feature_type,
    feature_comments
)
select  preftr_feature_zdb_id,
        preftr_feature_name,
        preftr_feature_abbrev,
        'TRANSGENIC_INSERTION',
        preftr_notes
 from pre_feature;
! echo "         into feature table."

-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  preftr_feature_zdb_id,
        'ZDB-PUB-071219-1'
 from pre_feature;
! echo "         into record_attribution table."


create table pre_feature_marker_relationship (
        prefmrel_feature_zdb_id varchar(50),
        prefmrel_marker_zdb_id varchar(50),
        prefmrel_type varchar(60)
);

insert into pre_feature_marker_relationship (prefmrel_feature_zdb_id,prefmrel_marker_zdb_id,prefmrel_type)
  select preftr_feature_zdb_id,preftr_construct,'contains phenotypic sequence feature'
    from pre_feature;
    
alter table pre_feature_marker_relationship add prefmrel_zdb_id varchar(50);

update pre_feature_marker_relationship set prefmrel_zdb_id = get_id('FMREL');


insert into zdb_active_data select prefmrel_zdb_id from pre_feature_marker_relationship;
! echo "         into zdb_active_data table."

-- load feature_marker_relationship table
insert into feature_marker_relationship (
    fmrel_zdb_id,
    fmrel_type,
    fmrel_ftr_zdb_id,
    fmrel_mrkr_zdb_id
)
select  prefmrel_zdb_id,
        prefmrel_type,
        prefmrel_feature_zdb_id,
        prefmrel_marker_zdb_id
 from pre_feature_marker_relationship;
! echo "         into feature_marker_relationship table."

-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  prefmrel_zdb_id,
        'ZDB-PUB-071219-1'
 from pre_feature_marker_relationship;
! echo "         into record_attribution table."

-- load feature_assay table
insert into feature_assay (
    featassay_feature_zdb_id,
    featassay_mutagen,
    featassay_mutagee
)
select  preftr_feature_zdb_id,
        preftr_mutagen,
        preftr_mutagee
 from pre_feature;
! echo "         into feature_assay table."

-- load int_data_source table
insert into int_data_source (
    ids_data_zdb_id,
    ids_source_zdb_id
)
select  preftr_feature_zdb_id,
        preftr_source
 from pre_feature;
! echo "         into int_data_source table."

create table pre_geno (
        pregeno_feature_id varchar(50) not null,
        pregeno_display_name varchar(255) not null,
        pregeno_display_handle varchar(255) not null
);

-- load pre_geno table
insert into pre_geno (
        pregeno_feature_id,
        pregeno_display_name,
        pregeno_display_handle
)
select  preftr_feature_zdb_id,
        preftr_feature_name,
        preftr_feature_abbrev || '[U,U,U]'
 from pre_feature;
 
! echo "         into pre_geno table."

alter table pre_geno add pregeno_geno_id varchar(50);

update pre_geno set pregeno_geno_id = get_id('GENO');

insert into zdb_active_data select pregeno_geno_id from pre_geno;

! echo "         into zdb_active_data table."

-- load genotype table
insert into genotype (
    geno_zdb_id,
    geno_display_name,
    geno_handle
)
select  pregeno_geno_id,
        pregeno_display_name,
        pregeno_display_handle
 from pre_geno;
 
! echo "         into genotype table."


-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_geno_id,
        'ZDB-PUB-071219-1'
 from pre_geno;
 
! echo "         into record_attribution table."


alter table pre_geno add pregeno_genofeat_id varchar(50);

update pre_geno set pregeno_genofeat_id = get_id('GENOFEAT');

alter table pre_geno add pregeno_zyg varchar(50);

update pre_geno set pregeno_zyg = 'ZDB-ZYG-070117-7';


insert into zdb_active_data select pregeno_genofeat_id from pre_geno;
! echo "         into zdb_active_data table."

-- load genotype_feature table
insert into genotype_feature (
    genofeat_zdb_id,
    genofeat_geno_zdb_id,
    genofeat_feature_zdb_id,
    genofeat_zygocity,
    genofeat_dad_zygocity,
    genofeat_mom_zygocity
)
select  pregeno_genofeat_id,
        pregeno_geno_id,
        pregeno_feature_id,
        pregeno_zyg,
        pregeno_zyg,
        pregeno_zyg
 from pre_geno;
 
! echo "         into genotype_feature table."

-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_genofeat_id,
        'ZDB-PUB-071219-1'
 from pre_geno;
 
! echo "         into record_attribution table."

create table pre_genox (
        pregenox_zdb_id varchar(50) not null,
        pregenox_geno_zdb_id varchar(50) not null,
        pregenox_exp_zdb_id varchar(50) not null
);


-- load pre_genox table
insert into pre_genox (
    pregenox_zdb_id,
    pregenox_geno_zdb_id,
    pregenox_exp_zdb_id
)
select  get_id('GENOX'),
        pregeno_geno_id,
        'ZDB-EXP-070511-5'
 from pre_geno;
 
! echo "         into pre_genox table."

insert into zdb_active_data select pregenox_zdb_id from pre_genox;

! echo "         into zdb_active_data table."

-- load genotype_experiment table
insert into genotype_experiment (
    genox_zdb_id,
    genox_geno_zdb_id,
    genox_exp_zdb_id
)
select pregenox_zdb_id,
       pregenox_geno_zdb_id,
       pregenox_exp_zdb_id
 from pre_genox;
 
! echo "         into genotype_experiment table."

-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregenox_zdb_id,
        'ZDB-PUB-071219-1'
 from pre_genox;
! echo "         into record_attribution table."

create table pre_xpatex (
        prexpatex_zdb_id varchar(50) not null,
        prexpatex_genox_zdb_id varchar(50) not null,
        prexpatex_source_zdb_id varchar(50) not null,
        prexpatex_gene_zdb_id varchar(50) not null,
        prexpatex_assay_name varchar(40) not null        
);

-- load pre_xpatex table
insert into pre_xpatex (
    prexpatex_zdb_id,
    prexpatex_genox_zdb_id,
    prexpatex_source_zdb_id,
    prexpatex_gene_zdb_id,
    prexpatex_assay_name
)
select  get_id('XPAT'),
        pregenox_zdb_id,
        'ZDB-PUB-071219-1',
        'ZDB-EFG-070117-1',
        'Intrinsic fluorescence'
 from pre_genox;
 
! echo "         into pre_xpatex table."


insert into zdb_active_data select prexpatex_zdb_id from pre_xpatex;

! echo "         into zdb_active_data table."


-- load expression_experiment table
insert into expression_experiment (
    xpatex_zdb_id,
    xpatex_genox_zdb_id,
    xpatex_source_zdb_id,
    xpatex_gene_zdb_id,
    xpatex_assay_name
)
select  prexpatex_zdb_id,
    prexpatex_genox_zdb_id,
    prexpatex_source_zdb_id,
    prexpatex_gene_zdb_id,
    prexpatex_assay_name
 from pre_xpatex;
 
! echo "         into expression_experiment table."

-- "ZDB-XPAT-" records have been added to record_attribution table by trigger at this time

create table pre_xpatres (
        pre_xpatres_xpatex_zdb_id varchar(50) not null,
        pre_xpatres_superterm_zdb_id varchar(50) not null,
        pre_xpatres_start_stg_zdb_id varchar(50) not null,
        pre_xpatres_end_stg_zdb_id varchar(50) not null,
        pre_xpatres_expression_found boolean not null        
);


-- load pre_xpatres table
insert into pre_xpatres (
    pre_xpatres_xpatex_zdb_id,
    pre_xpatres_superterm_zdb_id,
    pre_xpatres_start_stg_zdb_id,
    pre_xpatres_end_stg_zdb_id,
    pre_xpatres_expression_found
)
select  prexpatex_zdb_id,
        kk_ao,
        kk_stage,
        kk_stage,
        't'
 from KKawakami, pre_geno, pre_genox, pre_xpatex
 where kk_feature_name = pregeno_display_name
   and pregeno_geno_id = pregenox_geno_zdb_id
   and pregenox_zdb_id = prexpatex_genox_zdb_id;
 
! echo "         into pre_xpatres table."

alter table pre_xpatres add pre_xpatres_zdb_id varchar(50);

update pre_xpatres set pre_xpatres_zdb_id = get_id('XPATRES');

insert into zdb_active_data select pre_xpatres_zdb_id from pre_xpatres;

! echo "         into zdb_active_data table."


-- load expression_result table
insert into expression_result (
    xpatres_zdb_id,
    xpatres_xpatex_zdb_id,
    xpatres_superterm_zdb_id,
    xpatres_start_stg_zdb_id,
    xpatres_end_stg_zdb_id,
    xpatres_expression_found
)
select  pre_xpatres_zdb_id,
    pre_xpatres_xpatex_zdb_id,
    pre_xpatres_superterm_zdb_id,
    pre_xpatres_start_stg_zdb_id,
    pre_xpatres_end_stg_zdb_id,
    pre_xpatres_expression_found
 from pre_xpatres;
 
! echo "         into expression_result table."

-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pre_xpatres_zdb_id,
        'ZDB-PUB-071219-1'
 from pre_xpatres;
 
! echo "         into record_attribution table."

-- load expression_pattern_figure table
insert into expression_pattern_figure (xpatfig_fig_zdb_id, xpatfig_xpatres_zdb_id)
  select 'ZDB-FIG-080326-85', pre_xpatres_zdb_id
    from pre_xpatres;
 
! echo "         into expression_pattern_figure table."

drop table KKawakami;
drop table pre_feature;
drop table pre_feature_marker_relationship;
drop table pre_geno;
drop table pre_genox;
drop table pre_xpatex;
drop table pre_xpatres;

-- rollback work;

commit work;

execute function regen_genox();

execute function regen_names();

