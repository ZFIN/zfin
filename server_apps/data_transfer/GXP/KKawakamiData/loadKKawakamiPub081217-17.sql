-- loadKKawakamiPub081217-17.sql
-- One-time use script for FB case 5271 "another data load from a K.kawakami paper ZDB-PUB-081217-17"
-- Input file was provided by Ceri and pre-processed by Xiang
-- This script is kept for being adapted to future similar requests
-- Add 108 new feature records, 108 new genotype records, 66 new linkage and linkage_member records, 
-- and 208 new genotype_feature records
-- input: KKawakamiPub08121712.unl
-- output: a bunch of tables at ZFIN get new data inserted
-- The hard-coded data associated with this loading:
--   ZDB-ZYG-070117-7   (unknown zygocity)   
--   ZDB-PUB-081217-17   ( Urasaki et al, 2008)

begin work;

create temp table KKawakamidataSet2 (
        kk_feature_name_2 varchar(255) not null,
        kk_nkfuji70 varchar(50),
        kk_feature_type varchar(30) not null,
        kk_construct varchar(50) not null,
        kk_lg varchar(2),
        kk_note varchar(100) not null,
        kk_feature_abbrev varchar(70) not null
) with no log;


load from KKawakamiPub08121712.unl
 insert into KKawakamidataSet2;

create table pre_feature (
        preftr_feature_name varchar(255),
        preftr_nkfuji70 varchar(50),
        preftr_feature_type varchar(30),
        preftr_data_source varchar(50),
        preftr_mutagee varchar(20),
        preftr_mutagen varchar(20),        
        preftr_construct varchar(50),
        preftr_notes varchar(60),
        preftr_feature_abbrev varchar(70),
        preftr_lg varchar(2)
);

insert into pre_feature (
      preftr_feature_name,
      preftr_nkfuji70,
      preftr_feature_type,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_construct,
      preftr_notes,
      preftr_feature_abbrev,
      preftr_lg
      )
  select distinct mrkr_abbrev || kk_feature_name_2,
                  kk_nkfuji70,
                  kk_feature_type,
                  'ZDB-LAB-041019-1',
                  'embryos',
                  'DNA',
                  kk_construct,
                  kk_note,
                  kk_feature_name_2,
                  kk_lg
    from KKawakamidataSet2, marker
   where kk_construct = mrkr_zdb_id;
   

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
        'ZDB-PUB-081217-17'
 from pre_feature;
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
        preftr_data_source
 from pre_feature;
! echo "         into int_data_source table."

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
        'ZDB-PUB-081217-17'
 from pre_feature_marker_relationship;
! echo "         into record_attribution table."

create table pre_linkage (
        prelg_feature_id varchar(50) not null,
        prelg_or_lg varchar(2) not null,
        prelg_comments lvarchar not null,
        prelg_submitter varchar(50) not null
);

insert into pre_linkage (prelg_feature_id,prelg_or_lg,prelg_comments,prelg_submitter)
  select preftr_feature_zdb_id,
         preftr_lg,
         'Urasaki, A. et al. (2008, Proc. Natl. Acad. Sci. USA 105(50):19827-19832) determined the location of the insertion site using Ensembl Build Zv7.',
         'ZDB-PERS-030612-1'
    from pre_feature
   where preftr_lg is not null;

alter table pre_linkage add prelg_zdb_id varchar(50);

update pre_linkage set prelg_zdb_id = get_id('LINK');

insert into zdb_active_data select prelg_zdb_id from pre_linkage;
! echo "         into zdb_active_data table."

-- load linkage table
insert into linkage (lnkg_zdb_id, lnkg_or_lg, lnkg_comments, lnkg_submitter_zdb_id)
 select prelg_zdb_id, prelg_or_lg, prelg_comments, prelg_submitter
   from pre_linkage;
! echo "         into linkage table."   
   
-- load linkage_member table
insert into linkage_member (lnkgmem_linkage_zdb_id, lnkgmem_member_zdb_id)
 select prelg_zdb_id, prelg_feature_id
   from pre_linkage;
! echo "         into linkage_member table." 

-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  prelg_zdb_id,
        'ZDB-PUB-081217-17'
 from pre_linkage;
! echo "         into record_attribution table."

create table pre_geno (
        pregeno_feature_id varchar(50) not null,
        pregeno_display_name varchar(255) not null,
        pregeno_display_handle varchar(255) not null,
        pregeno_nick_name varchar(255) not null
);

-- load pre_geno table for those not associated with nkfuji70
insert into pre_geno (
        pregeno_feature_id,
        pregeno_display_name,
        pregeno_display_handle,
        pregeno_nick_name
)
select preftr_feature_zdb_id,
       preftr_feature_name,
       preftr_feature_abbrev || '[U,U,U]',
       preftr_feature_abbrev || '[U,U,U]'
  from pre_feature
 where preftr_nkfuji70 is null;
 
! echo "         into pre_geno table."

-- load pre_geno table for those associated with nkfuji70
insert into pre_geno (
        pregeno_feature_id,
        pregeno_display_name,
        pregeno_display_handle,
        pregeno_nick_name
)
select preftr_feature_zdb_id,
       feature_name || ';' || preftr_feature_name,
       feature_abbrev || '[U,U,U]' || ' ' || preftr_feature_abbrev || '[U,U,U]',
       feature_abbrev || '[U,U,U]' || ' ' || preftr_feature_abbrev || '[U,U,U]'
  from pre_feature, feature
 where preftr_nkfuji70 is not null
   and preftr_nkfuji70 = feature_zdb_id;
 
! echo "         into pre_geno table."

alter table pre_geno add pregeno_geno_id varchar(50);

update pre_geno set pregeno_geno_id = get_id('GENO');


insert into zdb_active_data select pregeno_geno_id from pre_geno;

! echo "         into zdb_active_data table."

-- load genotype table
insert into genotype (
    geno_zdb_id,
    geno_display_name,
    geno_handle,
    geno_nickname
)
select  pregeno_geno_id,
        pregeno_display_name,
        pregeno_display_handle,
        pregeno_nick_name
 from pre_geno;
 
! echo "         into genotype table."


-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_geno_id,
        'ZDB-PUB-081217-17'
 from pre_geno;
 
! echo "         into record_attribution table."

create table pre_geno_ftr_relationship (
        pregfrel_geno_zdb_id varchar(50),
        pregfrel_feature_zdb_id varchar(50),
        pregfrel_zygocity varchar(50),
        pregfrel_dad_zygocity varchar(50),
        pregfrel_mom_zygocity varchar(50)
);

-- load pre_geno_ftr_relationship table with records not having relationship with nkfuji70
insert into pre_geno_ftr_relationship (
    pregfrel_geno_zdb_id,
    pregfrel_feature_zdb_id,
    pregfrel_zygocity,
    pregfrel_dad_zygocity,
    pregfrel_mom_zygocity
)
select  pregeno_geno_id,
        pregeno_feature_id,
        'ZDB-ZYG-070117-7',
        'ZDB-ZYG-070117-7',
        'ZDB-ZYG-070117-7'
 from pre_geno;
 
-- load pre_geno_ftr_relationship table with records having relationship with nkfuji70 
insert into pre_geno_ftr_relationship (
    pregfrel_geno_zdb_id,
    pregfrel_feature_zdb_id,
    pregfrel_zygocity,
    pregfrel_dad_zygocity,
    pregfrel_mom_zygocity
)
select  pregeno_geno_id,
        preftr_nkfuji70,
        'ZDB-ZYG-070117-7',
        'ZDB-ZYG-070117-7',
        'ZDB-ZYG-070117-7'
 from pre_geno, pre_feature
where preftr_nkfuji70 is not null
  and pregeno_feature_id = preftr_feature_zdb_id;

alter table pre_geno_ftr_relationship add pregfrel_genofeat_id varchar(50);

update pre_geno_ftr_relationship set pregfrel_genofeat_id = get_id('GENOFEAT');

insert into zdb_active_data select pregfrel_genofeat_id from pre_geno_ftr_relationship;
! echo "         into zdb_active_data table."

-- load genotype_feature table for those not associated with nkfuji70
insert into genotype_feature (
    genofeat_zdb_id,
    genofeat_geno_zdb_id,
    genofeat_feature_zdb_id,
    genofeat_zygocity,
    genofeat_dad_zygocity,
    genofeat_mom_zygocity
)
select  pregfrel_genofeat_id,
        pregfrel_geno_zdb_id,
        pregfrel_feature_zdb_id,
        pregfrel_zygocity,
        pregfrel_dad_zygocity,
        pregfrel_mom_zygocity
 from pre_geno_ftr_relationship;
 
! echo "         into genotype_feature table."

-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregfrel_genofeat_id,
        'ZDB-PUB-081217-17'
 from pre_geno_ftr_relationship;
 
! echo "         into record_attribution table."


drop table pre_feature;
drop table pre_feature_marker_relationship;
drop table pre_linkage;
drop table pre_geno;
drop table pre_geno_ftr_relationship;

-- rollback work;

commit work;

execute function regen_names();

