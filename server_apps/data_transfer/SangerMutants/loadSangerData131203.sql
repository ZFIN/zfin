-- loadSangerData.sql
-- input: allelezfin.unl
-- Some of the hard-coded data associated with this loading:
-- TL background :ZDB-GENO-990623-2
-- genotypes are on a [2,1,1] background
-- zygocity:
-- ZDB-LAB-050412-2  Stemple Lab or ZDB-LAB-070815-1 cuppen Lab
-- ZDB-PUB-130425-4


begin work;

---creating temp table to load all of the input data form sanger
create temp table sanger_pre_input_known (
     sanger_preinput_feature_abbrev varchar(255),
     sanger_preinput_gene_zdb_id varchar (50),
     sanger_preinput_background varchar(50),
     sanger_preinpput_ftr_line_number varchar(50))
 with no log;

load from sangerInputWithoutDuplicates.unl insert into sanger_pre_input_known;
load from zfinGeneEnsdargMatches.unl insert into sanger_pre_input_known;

create temp table sanger_input_known (
     sanger_input_feature_abbrev varchar(255),
     sanger_input_gene_zdb_id varchar (50),
     sanger_input_background varchar(50),
     sanger_input_ftr_line_number varchar(50))
 with no log;

insert into sanger_input_known select distinct * from sanger_pre_input_known;




--updating temp table with ZFIN gene id's based on matches.




update sanger_input_known set sanger_input_gene_zdb_id = (select zrepld_new_zdb_id from zdb_replaced_data where sanger_input_gene_zdb_id=zrepld_old_zdb_id) where sanger_input_gene_zdb_id not in (select mrkr_zdb_id from marker);








create table pre_feature (
        preftr_feature_zdb_id varchar(50),
        preftr_feature_abbrev varchar(255),
        preftr_gene_zdb_id varchar (50),
        preftr_data_source varchar(50),
        preftr_mutagee varchar(20),
        preftr_mutagen varchar(20),        
        preftr_line_number varchar(70),
        preftr_lab_prefix_id int8
);

-- if the feature is not in ZFIN and no affected gene and only sa's
insert into pre_feature (
      preftr_feature_zdb_id,
      preftr_feature_abbrev,
      preftr_gene_zdb_id,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_line_number,
      preftr_lab_prefix_id
      )
  select distinct sanger_input_feature_abbrev,sanger_input_feature_abbrev,
                  sanger_input_gene_zdb_id,
                  'ZDB-LAB-050412-2',
                  'adult males',
                  'ENU',
                  sanger_input_ftr_line_number,
                  fp_pk_id
    from sanger_input_known, feature_prefix
     where fp_prefix = "sa"
     and sanger_input_feature_abbrev like 'sa%'
     and sanger_input_feature_abbrev not in (select feature_abbrev from feature);
unload to 'distinctftrcount.unl' select * from pre_feature;     


update pre_feature set preftr_feature_zdb_id = get_id('ALT');

unload to 'pre_feature.unl' select * from pre_feature;
! echo "         to pre_feature.unl"


insert into zdb_active_data select preftr_feature_zdb_id from pre_feature;
! echo "         into zdb_active_data table."


-- load feature table
insert into feature (
    feature_zdb_id,
    feature_name,
    feature_abbrev,
    feature_type,
    feature_lab_prefix_id,
    feature_line_number
)
select  preftr_feature_zdb_id,
        preftr_feature_abbrev,
        preftr_feature_abbrev,
        'POINT_MUTATION',
        preftr_lab_prefix_id,
        preftr_line_number
 from pre_feature;
 
! echo "         into feature table."

-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  preftr_feature_zdb_id,
        'ZDB-PUB-130425-4'
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
        prefmrel_zdb_id varchar(50),
        prefmrel_feature_zdb_id varchar(50),
        prefmrel_marker_zdb_id varchar(50),
        prefmrel_type varchar(60), prefmrel_feature_abbrev varchar(50)
);

-- relationship between the new features and the constructs
insert into pre_feature_marker_relationship (prefmrel_zdb_id,prefmrel_feature_zdb_id,prefmrel_marker_zdb_id,prefmrel_type,prefmrel_feature_abbrev)
  select preftr_feature_zdb_id,preftr_feature_zdb_id, preftr_gene_zdb_id, 'is allele of',preftr_feature_abbrev
    from pre_feature;
    
! echo "         into pre_feature_marker_relationship table."    

    
 

update pre_feature_marker_relationship set prefmrel_zdb_id = get_id('FMREL');
unload to 'pre_fmrel.unl' select distinct * from pre_feature_marker_relationship;
unload to notmarker select prefmrel_marker_zdb_id from pre_feature_marker_relationship where prefmrel_marker_zdb_id like 'ZDB-GENEP%';

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
        'ZDB-PUB-130425-4'
 from pre_feature_marker_relationship;
 
! echo "         into record_attribution table."


 
--insert into int_data_supplier (
--       idsup_data_zdb_id,
--       idsup_acc_num,
--       idsup_supplier_zdb_id
--) 
--select pregeno_geno_id,
--       pregeno_geno_id,
--       'ZDB-LAB-050412-2' 
--  from pre_geno;
  
--! echo "         into int_data_supplier table."




-- load int_data_supplier table with the new features
--insert into int_data_supplier (
--       idsup_data_zdb_id,
--       idsup_acc_num,
--       idsup_supplier_zdb_id
--) 
--select pregfrel_feature_zdb_id,
--       pregfrel_feature_zdb_id,
--       'ZDB-LAB-050412-2' 
--  from pre_geno_ftr_relationship;
  
--! echo "         into int_data_supplier table."

create table pre_db_link (
        predblink_dblink_zdb_id varchar(50),
        predblink_data_zdb_id varchar(50) not null,
        predblink_acc_num varchar(50) not null,
        predblink_acc_num_display varchar(50) not null,
        predblink_fdbcont_zdb_id varchar(50) not null
);

insert into pre_db_link (
        predblink_dblink_zdb_id,
        predblink_data_zdb_id,
        predblink_acc_num,
        predblink_acc_num_display,
        predblink_fdbcont_zdb_id)
  select distinct preftr_feature_zdb_id,preftr_feature_zdb_id, preftr_feature_abbrev, preftr_feature_abbrev, fdbcont_zdb_id 
    from pre_feature, foreign_db, foreign_db_contains 
   where fdbcont_fdb_db_id = fdb_db_pk_id 
     and fdb_db_name = 'ZMP';
     
! echo "         into pre_db_link table."  
     




update pre_db_link set predblink_dblink_zdb_id = get_id('DBLINK');

unload to 'pre_dblink.unl' select * from pre_db_link order by predblink_acc_num;

! echo "         to pre_db_link.unl"  
 
insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;

! echo "         into zdb_active_data table."  

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id) 
  select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id, predblink_acc_num_display, predblink_fdbcont_zdb_id 
    from pre_db_link; 

! echo "         into db_link table."      


insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)  
  select predblink_dblink_zdb_id,'ZDB-PUB-130425-4' from pre_db_link;

drop table pre_feature;
drop table pre_feature_marker_relationship;
drop table pre_db_link;

                                 
--rollback work;

commit work;


