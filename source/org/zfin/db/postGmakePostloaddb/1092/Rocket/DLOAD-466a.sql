--liquibase formatted sql
--changeset pm:DLOAD-466a

create table pre_feature (
        preftr_feature_name varchar(255),
        preftr_alias varchar(255),
        preftr_alias2 varchar(255) default null,
        preftr_data_source varchar(50),
        preftr_mutagee varchar(20),
        preftr_mutagen varchar(20),
        preftr_construct varchar(50),
        preftr_feature_abbrev varchar(70),
        preftr_line_number varchar(70),
        preftr_lab_prefix_id int8,
        preftr_known_insertion_site boolean default 't',
        preftr_tg_suffix varchar(5) default null
);

-- if the feature is not in ZFIN and no affected gene
insert into pre_feature (
      preftr_feature_name,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_construct,
      preftr_feature_abbrev,
      preftr_line_number,
      preftr_lab_prefix_id,
      preftr_known_insertion_site,preftr_tg_suffix
      )
  select distinct featureabbrev,
                  'ZDB-LAB-001206-5',
                  'embryos',
                  'DNA',
                  'ZDB-TGCONSTRCT-170913-6',
                  featureabbrev,
                  SUBSTR(featureabbrev,3,4),
                  342,
                  'f','Tg'
    from tmp_newfeatures where featureabbrev not in (Select ft_feature_abbrev from feature_tracking);




-- if the feature is not in ZFIN and there is affected gene

alter table pre_feAture add preftr_feature_zdb_id varchar(50);

update pre_feature set preftr_feature_zdb_id = get_id('ALT');






insert into zdb_active_data select preftr_feature_zdb_id from pre_feature;


-- load feature table
insert into feature (
    feature_zdb_id,
    feature_name,
    feature_abbrev,
    feature_type,
    feature_lab_prefix_id,
    feature_line_number,
    feature_tg_suffix,
    feature_known_insertion_site
)
select  preftr_feature_zdb_id,
        preftr_feature_name,
        preftr_feature_abbrev,
        'TRANSGENIC_INSERTION',
        preftr_lab_prefix_id,
        preftr_line_number,
        preftr_tg_suffix,
        preftr_known_insertion_site
 from pre_feature;



-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  preftr_feature_zdb_id,
        'ZDB-PUB-171006-9'
 from pre_feature;




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



-- load int_data_source table
insert into int_data_source (
    ids_data_zdb_id,
    ids_source_zdb_id
)
select  preftr_feature_zdb_id,
        preftr_data_source
 from pre_feature;




create table pre_feature_marker_relationship (
        prefmrel_feature_zdb_id varchar(50),
        prefmrel_marker_zdb_id varchar(50),
        prefmrel_type varchar(60)
);

-- relationship between the new features and the constructs
insert into pre_feature_marker_relationship (prefmrel_feature_zdb_id,prefmrel_marker_zdb_id,prefmrel_type)
  select preftr_feature_zdb_id, preftr_construct, 'contains innocuous sequence feature'
    from pre_feature;

update pre_feature_marker_relationship set prefmrel_zdb_id = get_id('FMREL');


insert into zdb_active_data select prefmrel_zdb_id from pre_feature_marker_relationship;

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



-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  prefmrel_zdb_id,
        'ZDB-PUB-171006-9'
 from pre_feature_marker_relationship;


create table pre_geno (
        pregeno_feature_id varchar(50) not null,
        pregeno_display_name varchar(255) not null,
        pregeno_handle varchar(255) not null,
        pregeno_nick_name varchar(255) not null
);

-- load pre_geno table for those features newly added by this script and not having affected gene
insert into pre_geno (
        pregeno_feature_id,
        pregeno_display_name,
        pregeno_handle,
        pregeno_nick_name
)
select preftr_feature_zdb_id,
       preftr_feature_name ,
       preftr_feature_abbrev  ,
       preftr_feature_abbrev
  from pre_feature;



-- load pre_geno table for those features newly added by this script and having affected gene
delete from pre_geno
 where exists (select "x" from genotype
                         where geno_handle = pregeno_handle);


alter table pre_geno add pregeno_geno_id varchar(50);

update pre_geno set pregeno_geno_id = get_id('GENO');

alter table pre_geno add pregeno_fish_id varchar(50);

update pre_geno set pregeno_fish_id = get_id('FISH');




insert into zdb_active_data select pregeno_geno_id from pre_geno;



-- load genotype table
insert into genotype (
    geno_zdb_id,
    geno_display_name,
    geno_handle,
    geno_nickname
)
select  pregeno_geno_id,
        pregeno_display_name,
        pregeno_handle,
        pregeno_nick_name
 from pre_geno;




-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_geno_id,
        'ZDB-PUB-171006-9'
 from pre_geno;



insert into zdb_active_data select pregeno_fish_id from pre_geno;



-- load fish table
insert into fish (
    fish_zdb_id,
    fish_genotype_zdb_id,
    fish_name,
    fish_handle
)
select  pregeno_fish_id,
        pregeno_geno_id,
        pregeno_display_name,
        pregeno_handle
 from pre_geno;




-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_fish_id,
        'ZDB-PUB-171006-9'
 from pre_geno;




create table pre_geno_ftr_relationship (
        pregfrel_geno_zdb_id varchar(50),
        pregfrel_feature_zdb_id varchar(50),
        pregfrel_zygocity varchar(50),
        pregfrel_dad_zygocity varchar(50),
        pregfrel_mom_zygocity varchar(50)
);

-- load pre_geno_ftr_relationship table
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




alter table pre_geno_ftr_relationship add pregfrel_genofeat_id varchar(50);

update pre_geno_ftr_relationship set pregfrel_genofeat_id = get_id('GENOFEAT');

insert into zdb_active_data select pregfrel_genofeat_id from pre_geno_ftr_relationship;



-- load genotype_feature table
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



-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregfrel_genofeat_id,
        'ZDB-PUB-171006-9'
 from pre_geno_ftr_relationship;

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
) values ('ZDB-TGCONSTRCT-170913-6','ZDB-PUB-171006-9');

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
) values ('ZDB-EFG-070117-1','ZDB-PUB-171006-9');

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
) values ('ZDB-EFG-080214-1','ZDB-PUB-171006-9');


drop table tmp_newfeatures;

drop table pre_feature;
drop table pre_feature_marker_relationship;

drop table pre_geno;
drop table pre_geno_ftr_relationship;

