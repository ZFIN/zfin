--liquibase formatted sql
--changeset pm:HBurgess

create table pre_feature (
        preftr_feature_name varchar(255),
        preftr_mutagee varchar(20),
        preftr_mutagen varchar(20),
        preftr_construct text,
        preftr_feature_abbrev varchar(70),
        preftr_line_number varchar(70),
        preftr_lab_prefix_id int8,
        preftr_tg_suffix varchar(5) default null,preftr_otherPub varchar(50), preftr_otherftr varchar(50),preftr_otherftrAbbrev varchar(70)
);


insert into pre_feature (
      preftr_feature_name,
      preftr_mutagee,
      preftr_mutagen,
      preftr_construct,
      preftr_feature_abbrev,
      preftr_line_number,
      preftr_lab_prefix_id,
      preftr_tg_suffix,preftr_otherPub,preftr_otherftr,preftr_otherftrAbbrev
      )
  select distinct
                  feature_abb,
                  'embryos',
                  'DNA',
                  construct_id,
                  feature_abb,
                  line_num,187,'Et',pub_id, other_feature_id,feature_abbrev

    from feature_data,feature where feature_abb not in (select feature_abbrev from feature) and other_feature_id=feature_zdb_id;


alter table pre_feature add preftr_feature_zdb_id varchar(50);

update pre_feature set preftr_feature_zdb_id = get_id('ALT');

insert into zdb_active_data select preftr_feature_zdb_id from pre_feature;
insert into feature (
    feature_zdb_id,
    feature_name,
    feature_abbrev,
    feature_type,
    feature_lab_prefix_id,
    feature_line_number,
    feature_tg_suffix,feature_name_order,feature_abbrev_order
)
select  preftr_feature_zdb_id,
        preftr_feature_name,
        preftr_feature_abbrev,
        'TRANSGENIC_INSERTION',
        preftr_lab_prefix_id,
        preftr_line_number,
        preftr_tg_suffix,preftr_feature_name,
        preftr_feature_abbrev
 from pre_feature;


insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  preftr_feature_zdb_id,
        'ZDB-PUB-180514-4'
 from pre_feature;
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  preftr_feature_zdb_id,
        preftr_otherPub
 from pre_feature;



insert into feature_assay (
    featassay_feature_zdb_id,
    featassay_mutagen,
    featassay_mutagee
)
select  preftr_feature_zdb_id,
        preftr_mutagen,
        preftr_mutagee
 from pre_feature;



create table pre_feature_marker_relationship (
        prefmrel_feature_zdb_id text,
        prefmrel_marker_zdb_id text,
        prefmrel_type varchar(60)
);


insert into pre_feature_marker_relationship (prefmrel_feature_zdb_id,prefmrel_marker_zdb_id,prefmrel_type)
  select preftr_feature_zdb_id, preftr_construct, 'contains innocuous sequence feature'
    from pre_feature;




alter table pre_feature_marker_relationship add prefmrel_zdb_id text;

update pre_feature_marker_relationship set prefmrel_zdb_id = get_id('FMREL');


insert into zdb_active_data select prefmrel_zdb_id from pre_feature_marker_relationship;



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




insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  prefmrel_zdb_id,
        'ZDB-PUB-180514-4'
 from pre_feature_marker_relationship;
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  prefmrel_zdb_id,
        preftr_otherPub
from pre_feature_marker_relationship, pre_feature
where prefmrel_feature_zdb_id=preftr_feature_zdb_id;



create table pre_geno (
        pregeno_feature_id text not null,
        pregeno_display_name varchar(255) not null,
        pregeno_handle varchar(255) not null,
        pregeno_nick_name varchar(255) not null
);


insert into pre_geno (
        pregeno_feature_id,
        pregeno_display_name,
        pregeno_handle,
        pregeno_nick_name
)
select preftr_feature_zdb_id,
       preftr_otherftrAbbrev||'; '||preftr_feature_abbrev,
       preftr_otherftrAbbrev||'[U,U,U] '||preftr_feature_abbrev||'[U,U,U]TL',
       preftr_otherftrAbbrev||'; '||preftr_feature_abbrev
  from pre_feature;



alter table pre_geno add pregeno_geno_id varchar(50);

update pre_geno set pregeno_geno_id = get_id('GENO');

alter table pre_geno add pregeno_fish_id varchar(50);

update pre_geno set pregeno_fish_id = get_id('FISH');



insert into zdb_active_data select pregeno_geno_id from pre_geno;


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


insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_geno_id,
        'ZDB-PUB-180514-4'
 from pre_geno;

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_geno_id,
        pre_feature.preftr_otherPub
 from pre_geno,pre_feature
where pre_geno.pregeno_feature_id=pre_feature.preftr_feature_zdb_id;

insert into zdb_active_data select pregeno_fish_id from pre_geno;



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



insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_fish_id,
        'ZDB-PUB-180514-4'
 from pre_geno;
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_fish_id,
        pre_feature.preftr_otherPub
 from pre_geno,pre_feature
where pre_geno.pregeno_feature_id=pre_feature.preftr_feature_zdb_id;



insert into genotype_background (
    genoback_geno_zdb_id,
    genoback_background_zdb_id
)
select  pregeno_geno_id,
        'ZDB-GENO-990623-2'
 from pre_geno;


create table pre_geno_ftr_relationship (
        pregfrel_geno_zdb_id text,
        pregfrel_feature_zdb_id text,
        pregfrel_zygocity text,
        pregfrel_dad_zygocity text,
        pregfrel_mom_zygocity text
);


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


insert into pre_geno_ftr_relationship (
    pregfrel_geno_zdb_id,
    pregfrel_feature_zdb_id,
    pregfrel_zygocity,
    pregfrel_dad_zygocity,
    pregfrel_mom_zygocity
)
select  pregeno_geno_id,
        preftr_otherftr,
        'ZDB-ZYG-070117-7',
        'ZDB-ZYG-070117-7',
        'ZDB-ZYG-070117-7'
from pre_geno,pre_feature where pregeno_feature_id=preftr_feature_zdb_id;


alter table pre_geno_ftr_relationship add pregfrel_genofeat_id text;

update pre_geno_ftr_relationship set pregfrel_genofeat_id = get_id('GENOFEAT');

insert into zdb_active_data select pregfrel_genofeat_id from pre_geno_ftr_relationship;


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


insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregfrel_genofeat_id,
        'ZDB-PUB-180514-4'
 from pre_geno_ftr_relationship;
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregfrel_genofeat_id,
        pre_feature.preftr_otherPub
 from pre_geno_ftr_relationship,pre_feature
where pre_geno_ftr_relationship.pregfrel_feature_zdb_id=pre_feature.preftr_feature_zdb_id;









drop table pre_feature;
drop table pre_feature_marker_relationship;

drop table pre_geno;
drop table pre_geno_ftr_relationship;



update genotype set geno_handle = geno_handle;

