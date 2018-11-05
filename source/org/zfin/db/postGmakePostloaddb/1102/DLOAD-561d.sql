--liquibase formatted sql
--changeset pm:DLOAD-561d


create table pre_gene (
        gene_zdb_id varchar(50),
        gene_abbrev varchar(255),
        gene_name varchar(255),
        featureabbrev varchar(50));

insert into pre_gene (gene_zdb_id,gene_abbrev,gene_name,featureabbrev) select geneabbrev,geneabbrev,geneabbrev,allele from featurezmp;
update pre_gene set gene_zdb_id=get_id('GENE');


insert into zdb_active_data select gene_zdb_id from pre_gene;


insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id) select dblink_id,'ZDB-PUB-130425-4' from pre_gene;

insert into marker(mrkr_zdb_id, mrkr_abbrev, mrkr_name, mrkr_owner,mrkr_type) select distinct gene_zdb_id,gene_abbrev,gene_name,'ZDB-PERS-040722-4','GENE' from pre_gene;


insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) select distinct gene_zdb_id, 'ZDB-PUB-130425-4' from pre_gene;



create table pre_feature (
        preftr_feature_abbrev varchar(255),
        preftr_gene_zdb_id varchar (50),
        preftr_data_source varchar(50),
        preftr_mutagee varchar(20),
        preftr_mutagen varchar(20),
        preftr_line_number varchar(70),
        preftr_lab_prefix_id int8
);

insert into pre_feature (
      preftr_feature_abbrev,
      preftr_gene_zdb_id,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_line_number,
      preftr_lab_prefix_id
      )
  select distinct featureabbrev,
                  gene_zdb_id,
                  'ZDB-LAB-050412-2',
                  'adult males',
                  'ENU',
                  SUBSTR(featureabbrev,3),
                  fp_pk_id
    from pre_gene, feature_prefix
     where fp_prefix = 'sa'
     and featureabbrev like 'sa%'
     and featureabbrev not in (select feature_abbrev from feature);




alter table pre_feature add preftr_feature_zdb_id varchar(50);

update pre_feature set preftr_feature_zdb_id = get_id('ALT');




insert into zdb_active_data select preftr_feature_zdb_id from pre_feature;



insert into feature (
    feature_zdb_id,
    feature_name,
    feature_abbrev,
    feature_type,
    feature_lab_prefix_id,
    feature_line_number,feature_name_order,feature_abbrev_order
)
select  distinct preftr_feature_zdb_id,
        preftr_feature_abbrev,
        preftr_feature_abbrev,
        'POINT_MUTATION',
        preftr_lab_prefix_id,
        preftr_line_number,preftr_feature_abbrev,
        preftr_feature_abbrev
 from pre_feature;

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  preftr_feature_zdb_id,
        'ZDB-PUB-130425-4'
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

insert into pre_feature_marker_relationship (prefmrel_feature_zdb_id,prefmrel_marker_zdb_id,prefmrel_type)
  select preftr_feature_zdb_id, preftr_gene_zdb_id, 'is allele of'
    from pre_feature;


alter table pre_feature_marker_relationship add prefmrel_zdb_id varchar(50);

update pre_feature_marker_relationship set prefmrel_zdb_id = get_id('FMREL');

insert into zdb_active_data select prefmrel_zdb_id from pre_feature_marker_relationship;

select * from pre_feature_marker_relationship where prefmrel_marker_zdb_id not like '%GENE%';

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
        'ZDB-PUB-130425-4'
 from pre_feature_marker_relationship;

create table pre_db_link (
        predblink_data_zdb_id varchar(50) not null,
        predblink_acc_num varchar(50) not null,
        predblink_acc_num_display varchar(50) not null,
        predblink_fdbcont_zdb_id varchar(50) not null
);

insert into pre_db_link (
        predblink_data_zdb_id,
        predblink_acc_num,
        predblink_acc_num_display,
        predblink_fdbcont_zdb_id)
  select distinct preftr_feature_zdb_id, preftr_feature_abbrev, preftr_feature_abbrev, fdbcont_zdb_id
    from pre_feature, foreign_db, foreign_db_contains
   where fdbcont_fdb_db_id = fdb_db_pk_id
     and fdb_db_name = 'ZMP';

alter table pre_db_link add predblink_dblink_zdb_id varchar(50);

update pre_db_link set predblink_dblink_zdb_id = get_id('DBLINK');


insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id)
  select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id, predblink_acc_num_display, predblink_fdbcont_zdb_id
    from pre_db_link;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select predblink_dblink_zdb_id,'ZDB-PUB-130425-4' from pre_db_link;



drop table pre_gene;
drop table pre_feature;
drop table pre_feature_marker_relationship;
drop table pre_db_link;