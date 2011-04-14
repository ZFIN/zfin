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
create temp table KKawakamidataSet2 (
        kk_feature_name_2 varchar(255) not null,
        kk_nkfuji70 varchar(50),
        kk_feature_type varchar(30) not null,
        kk_construct varchar(50) not null,
        kk_lg varchar(2),
        kk_genbank varchar(100) not null,
        kk_feature_abbrev varchar(70) not null
) with no log;


load from  KawakamiPub08121712gb.unl
 insert into KKawakamidataSet2;

create table pre_feature (
        preftr_feature_name varchar(255),
        preftr_nkfuji70 varchar(50),
        preftr_feature_type varchar(30),
        preftr_data_source varchar(50),
        preftr_mutagee varchar(20),
        preftr_mutagen varchar(20),
        preftr_construct varchar(50),
        preftr_genbank varchar(60),
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
      preftr_genbank,
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
                  kk_genbank,
                  kk_feature_name_2,
                  kk_lg
    from KKawakamidataSet2, marker
   where kk_construct = mrkr_zdb_id;

   

create table pre_db_link (
        predblink_data_zdb_id varchar(50),
        predblink_acc_num varchar(50),
        predblink_acc_num_display varchar(50),
        predblink_dblink_zdb_id varchar(50),
        predblink_genbank_zdb_id varchar(50)
);
alter table pre_feature add preftr_feature_zdb_id varchar(50);

update pre_feature set preftr_feature_zdb_id = (select feature_zdb_id from feature where preftr_feature_abbrev=feature_abbrev);


insert into pre_db_link (
        predblink_data_zdb_id,
        predblink_acc_num,
        predblink_acc_num_display,
        predblink_dblink_zdb_id,
        predblink_genbank_zdb_id)
  select distinct preftr_feature_zdb_id,preftr_genbank,preftr_genbank,get_id('DBLINK'),'ZDB-FDBCONT-040412-36' from pre_feature;

unload to 'genbank.unl' select * from pre_db_link;
 
insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id) select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id,  predblink_acc_num,'ZDB-FDBCONT-040412-36' from pre_db_link; 

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)  select predblink_dblink_zdb_id,'ZDB-PUB-081217-17' from pre_db_link;


drop table KKawakamidataSet2;
drop table pre_feature;
drop table pre_db_link;

--rollback work;

commit work;


