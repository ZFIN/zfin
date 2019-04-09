--liquibase formatted sql
--changeset pm:DLOAD-621c







delete from featuredata where alleleid in (select feature_abbrev from feature);



insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) select distinct feature_zdb_id, 'ZDB-PUB-130425-4' from featuredata, feature where feature_abbrev=alleleid and feature_zdb_id not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-130425-4');



 

create table pre_feature (
        preftr_feature_abbrev varchar(255),
        preftr_ensdarg_id varchar (50),

        preftr_data_source varchar(50),
        preftr_mutagee varchar(20),
        preftr_mutagen varchar(20),        
        preftr_line_number varchar(70),
        preftr_lab_prefix_id int8
);

insert into pre_feature (
      preftr_feature_abbrev,
      preftr_ensdarg_id,

      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_line_number,
      preftr_lab_prefix_id
      )
  select distinct alleleid,
                  ensdarg1,
                  'ZDB-LAB-050412-2',
                  'adult males',
                  'ENU',
                  SUBSTR(alleleid,3),
                  fp_pk_id
    from featuredata, feature_prefix
     where fp_prefix = 'sa'
     and alleleid like 'sa%'
     and alleleid not in (select feature_abbrev from feature) and ensdarg2='' ;

     insert into pre_feature (
      preftr_feature_abbrev,
      preftr_ensdarg_id,

      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_line_number,
      preftr_lab_prefix_id
      )
  select distinct alleleid,
                  ensdarg2,
                  'ZDB-LAB-050412-2',
                  'adult males',
                  'ENU',
                  SUBSTR(alleleid,3),
                  fp_pk_id
    from featuredata, feature_prefix
     where fp_prefix = 'sa'
     and alleleid like 'sa%'
     and alleleid not in (select feature_abbrev from feature) and ensdarg2!='' ;




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


create table pre_extnote (
        extnote_id varchar(50) not null,
        extnote_data_id varchar(255) not null,
        extnote_note varchar(255) not null,
        extnote_notetype varchar(255) not null,
        extnote_source_id varchar(50)
);

insert into pre_extnote (
       extnote_id ,
        extnote_data_id,
        extnote_note,
        extnote_notetype ,
        extnote_source_id
)
select distinct preftr_feature_zdb_id, preftr_feature_zdb_id,'This alleleid was mapped to ' || preftr_ensdarg_id|| ' by the ZMP Project','feature','ZDB-PUB-130425-4' from pre_feature where preftr_ensdarg_id!='';
update pre_extnote set extnote_id=get_id('EXTNOTE');
insert into zdb_active_data select extnote_id from pre_extnote;
insert into external_note select * from pre_extnote;

drop table pre_feature;

drop table pre_db_link;

drop table pre_extnote;