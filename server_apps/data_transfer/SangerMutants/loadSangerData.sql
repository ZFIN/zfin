-- loadSangerData.sql
-- input: allelezfin.unl
-- Some of the hard-coded data associated with this loading:
-- TL background :ZDB-GENO-990623-2
-- genotypes are on a [2,1,1] background
-- zygocity:
-- ZDB-LAB-050412-2  Stemple Lab or ZDB-LAB-070815-1 cuppen Lab
-- ZDB-PUB-120207-1


begin work;

---creating temp table to load all of the input data form sanger
create temp table sanger_pre_input_known (
     sanger_preinput_feature_abbrev varchar(255),
     sanger_preinput_gene_zdb_id varchar (50),
     sanger_preinput_background varchar(50),
     sanger_preinput_line_number varchar(70)
) with no log;

load from pre_load_input_known.txt insert into sanger_pre_input_known;

create temp table sanger_input_known (
     sanger_input_feature_abbrev varchar(255),
     sanger_input_gene_zdb_id varchar (50),
     sanger_input_background varchar(50),
     sanger_input_line_number varchar(70)
) with no log;

insert into sanger_input_known select distinct * from sanger_pre_input_known;

create temp table sanger_pre_input_ensdarg (
     sanger_preinput_ens_feature_abbrev varchar(255),
     sanger_preinput_ens_gene_zdb_id varchar (50),
     sanger_preinput_ens_background varchar(50),
     sanger_preinput_ens_line_number varchar(70)
) with no log;

load from pre_load_input_ensdarg.txt insert into sanger_pre_input_ensdarg;

create temp table sanger_input_ensdarg (
     sanger_input_ens_feature_abbrev varchar(255),
     sanger_input_ens_gene_zdb_id varchar (50),
     sanger_input_ens_background varchar(50),
     sanger_input_ens_line_number varchar(70)
) with no log;

insert into sanger_input_ensdarg select distinct * from sanger_pre_input_ensdarg;

---creating temp table to load all Esnsdarg-zfin gene matches as curated by Leyla

create temp table ensdarg_matches (
     ensdarg_matches_ensdarg_id varchar(50),
     ensdarg_matches_zfin_id varchar(50)
) with no log;

load from EnsdargMatches.txt insert into ensdarg_matches;


--updating temp table with ZFIN gene id's based on matches.



update sanger_input_ensdarg set sanger_input_ens_gene_zdb_id = (select distinct ensdarg_matches_zfin_id from ensdarg_matches where sanger_input_ens_gene_zdb_id=ensdarg_matches_ensdarg_id and sanger_input_ens_gene_zdb_id like '%ENSDARG%') where sanger_input_ens_gene_zdb_id in (select ensdarg_matches_ensdarg_id from ensdarg_matches);


update sanger_input_known set sanger_input_gene_zdb_id = (select zrepld_new_zdb_id from zdb_replaced_data where sanger_input_gene_zdb_id=zrepld_old_zdb_id) where sanger_input_gene_zdb_id not in (select mrkr_zdb_id from marker where mrkr_type='GENE');


---creating temp table to create new ZFIN unnamed genes for those with no matches

create table pre_gene (
        gene_zdb_id varchar(50),
        dblink_id varchar(50), 
        ensdarg_id varchar(50),
        gene_abbrev varchar(255),
        gene_name varchar(255),
        feature_abbrev varchar(50)); 

unload to "geneunnamed.unl" select distinct sanger_input_ens_gene_zdb_id,sanger_input_ens_gene_zdb_id,sanger_input_ens_gene_zdb_id,  "unm_"||sanger_input_ens_feature_abbrev, "un-named "||sanger_input_ens_feature_abbrev,sanger_input_ens_feature_abbrev  from sanger_input_ensdarg where sanger_input_ens_gene_zdb_id like '%ENSDARG%';

load from geneunnamed.unl insert into pre_gene;


unload to pre_gene.unl select * from pre_gene;
unload to 'inzfinunm' select gene_abbrev from pre_gene where gene_abbrev in (select mrkr_abbrev from marker where mrkr_type='GENE' and mrkr_abbrev like 'unm_%');
delete from pre_gene
 where exists (select "x" from marker
                         where mrkr_abbrev = gene_abbrev);

update pre_gene set gene_zdb_id=get_id('GENE');
update pre_gene set dblink_id=get_id('DBLINK');

insert into zdb_active_data select gene_zdb_id from pre_gene;
insert into zdb_active_data select dblink_id from pre_gene;

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id) select dblink_id,"ZDB-PUB-020723-5" from pre_gene;

insert into marker(mrkr_zdb_id, mrkr_abbrev, mrkr_name, mrkr_owner,mrkr_type) select distinct gene_zdb_id,gene_abbrev,gene_name,"ZDB-PERS-040722-4","GENE" from pre_gene;

insert
into
   marker_history
   (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, mhist_data, mhist_mrkr_name_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date)
VALUES
   select
      distinct get_Id('NOMEN'),
      gene_zdb_id,
      'assigned',
      'Not Specified',
      current,
      gene_name,
      gene_abbrev
   from
      pre_gene;

insert into db_link (dblink_linked_recid,dblink_acc_num,dblink_zdb_id,dblink_fdbcont_zdb_id) select distinct gene_zdb_id,ensdarg_id,dblink_id,"ZDB-FDBCONT-061018-1" from pre_gene; 

--updatng inuput table with gene ids 

--update sanger_input_ensdarg set sanger_input_ens_gene_zdb_id = (select distinct gene_zdb_id  from pre_gene where sanger_input_ens_gene_zdb_id=ensdarg_id and sanger_input_ens_gene_zdb_id like '%ENSDARG%') where sanger_input_ens_gene_zdb_id in (select ensdarg_id from pre_gene);

update sanger_input_ensdarg set sanger_input_ens_gene_zdb_id = (select distinct gene_zdb_id  from pre_gene where sanger_input_ens_feature_abbrev=feature_abbrev and sanger_input_ens_gene_zdb_id like '%ENSDARG%') where sanger_input_ens_gene_zdb_id in (select ensdarg_id from pre_gene);
insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) select distinct sanger_input_gene_zdb_id, 'ZDB-PUB-120207-1' from sanger_input_known where sanger_input_gene_zdb_id not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-120207-1');

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) select distinct feature_zdb_id, 'ZDB-PUB-120207-1' from sanger_input_known, feature where feature_abbrev=sanger_input_feature_abbrev and feature_zdb_id not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-120207-1');

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) select fmrel_zdb_id , 'ZDB-PUB-120207-1' from feature, feature_marker_relationship where feature_zdb_id=fmrel_ftr_zdb_id and feature_abbrev in (select sanger_input_feature_abbrev from sanger_input_known) and fmrel_zdb_id not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-120207-1');

 

create table pre_feature (
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
      preftr_feature_abbrev,
      preftr_gene_zdb_id,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_line_number,
      preftr_lab_prefix_id
      )
  select distinct sanger_input_feature_abbrev,
                  sanger_input_gene_zdb_id,
                  'ZDB-LAB-050412-2',
                  'embryos',
                  'DNA',
                  sanger_input_line_number,
                  fp_pk_id
    from sanger_input_known, feature_prefix
     where fp_prefix = "sa"
     and sanger_input_feature_abbrev like 'sa%'
     and sanger_input_feature_abbrev not in (select feature_abbrev from feature);

insert into pre_feature (
      preftr_feature_abbrev,
      preftr_gene_zdb_id,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_line_number,
      preftr_lab_prefix_id
      )
  select distinct sanger_input_feature_abbrev,
                  sanger_input_gene_zdb_id,
                  'ZDB-LAB-070815-1',
                  'embryos',
                  'DNA',
                  sanger_input_line_number,
                  fp_pk_id
    from sanger_input_known, feature_prefix
     where fp_prefix = "hu"
     and sanger_input_feature_abbrev like 'hu%'
     and sanger_input_feature_abbrev not in (select feature_abbrev from feature);

insert into pre_feature (
      preftr_feature_abbrev,
      preftr_gene_zdb_id,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_line_number,
      preftr_lab_prefix_id
      )
  select distinct sanger_input_ens_feature_abbrev,
                  sanger_input_ens_gene_zdb_id,
                  'ZDB-LAB-050412-2',
                  'adult males',
                  'ENU',
                  sanger_input_ens_line_number,
                  fp_pk_id
    from sanger_input_ensdarg, feature_prefix
     where fp_prefix = "sa"
     and sanger_input_ens_feature_abbrev like 'sa%'
     and sanger_input_ens_feature_abbrev not in (select feature_abbrev from feature);

insert into pre_feature (
      preftr_feature_abbrev,
      preftr_gene_zdb_id,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_line_number,
      preftr_lab_prefix_id
      )
  select distinct sanger_input_ens_feature_abbrev,
                  sanger_input_ens_gene_zdb_id,
                  'ZDB-LAB-070815-1',
                  'adult males',
                  'ENU',
                  sanger_input_ens_line_number,
                  fp_pk_id
    from sanger_input_ensdarg, feature_prefix
     where fp_prefix = "hu"
     and sanger_input_ens_feature_abbrev like 'hu%'
     and sanger_input_ens_feature_abbrev not in (select feature_abbrev from feature);

unload to 'distinctftrcount.unl' select * from pre_feature;     

alter table pre_feature add preftr_feature_zdb_id varchar(50);

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
        'ZDB-PUB-120207-1'
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

-- relationship between the new features and the constructs
insert into pre_feature_marker_relationship (prefmrel_feature_zdb_id,prefmrel_marker_zdb_id,prefmrel_type)
  select preftr_feature_zdb_id, preftr_gene_zdb_id, 'is allele of'
    from pre_feature;
    
! echo "         into pre_feature_marker_relationship table."    

    
 
alter table pre_feature_marker_relationship add prefmrel_zdb_id varchar(50);

update pre_feature_marker_relationship set prefmrel_zdb_id = get_id('FMREL');
unload to 'prefmrel' select distinct * from pre_feature_marker_relationship;
unload to 'nomarker' select distinct prefmrel_marker_zdb_id from pre_feature_marker_relationship where prefmrel_marker_zdb_id not in (select mrkr_zdb_id from marker);

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
        'ZDB-PUB-120207-1'
 from pre_feature_marker_relationship;
 
! echo "         into record_attribution table."


create table pre_geno (
        pregeno_genozdbid varchar(50),
        pregeno_display_name varchar(255),
        pregeno_handle varchar(255),
        pregeno_nick_name varchar(255),
        pregeno_is_wildtype boolean,
        pregeno_feature varchar(50)
);

-- load pre_geno table for those features newly added by this script and not having affected gene
insert into pre_geno (
        pregeno_is_wildtype,
        pregeno_feature
)
select 'f',
       preftr_feature_zdb_id
  from pre_feature;
update pre_geno set pregeno_genozdbid=get_id('GENO');

 
! echo "         into pre_geno table."


insert into zdb_active_data select pregeno_genozdbid from pre_geno;

! echo "         into zdb_active_data table."

-- load genotype table
insert into genotype (
    geno_zdb_id,
    geno_display_name,
    geno_handle,
    geno_nickname,
    geno_is_wildtype
)
select  pregeno_genozdbid,
        pregeno_genozdbid,
        pregeno_genozdbid,
        pregeno_genozdbid,
        pregeno_is_wildtype
 from pre_geno;
 
! echo "         into genotype table."


-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  pregeno_genozdbid,
        'ZDB-PUB-120207-1'
 from pre_geno;
 
! echo "         into record_attribution table."


-- load genotype_background table
insert into genotype_background (
    genoback_geno_zdb_id,
    genoback_background_zdb_id
)
select  pregeno_genozdbid,
        'ZDB-GENO-990623-2'
 from pre_geno;
 
! echo "         into genotype_background table."

-- load int_data_supplier table with the new genotypes
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
select  pregeno_genozdbid,
        pregeno_feature,
        'ZDB-ZYG-070117-1',
        'ZDB-ZYG-070117-2',
        'ZDB-ZYG-070117-2'
 from pre_geno;
 
! echo "         into pre_geno_ftr_relationship table."

                            
alter table pre_geno_ftr_relationship add pregfrel_genofeat_id varchar(50);

update pre_geno_ftr_relationship set pregfrel_genofeat_id = get_id('GENOFEAT');

insert into zdb_active_data select pregfrel_genofeat_id from pre_geno_ftr_relationship;

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
        'ZDB-PUB-120207-1'
 from pre_geno_ftr_relationship;
 
! echo "         into record_attribution table."                            


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
     
! echo "         into pre_db_link table."  
     



alter table pre_db_link add predblink_dblink_zdb_id varchar(50);

update pre_db_link set predblink_dblink_zdb_id = get_id('DBLINK');

unload to 'pre_db_link.unl' select * from pre_db_link order by predblink_acc_num;

! echo "         to pre_db_link.unl"  
 
insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;

! echo "         into zdb_active_data table."  

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id) 
  select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id, predblink_acc_num_display, predblink_fdbcont_zdb_id 
    from pre_db_link; 

! echo "         into db_link table."      


insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)  
  select predblink_dblink_zdb_id,'ZDB-PUB-120207-1' from pre_db_link;

! echo "         into record_attribution table."  
update pre_geno set pregeno_display_name=get_genotype_display(pregeno_genozdbid);

update pre_geno set pregeno_handle=get_genotype_handle(pregeno_genozdbid);

update pre_geno set pregeno_nick_name=get_genotype_handle(pregeno_genozdbid);

update genotype set geno_display_name=(select pregeno_display_name from pre_geno where geno_zdb_id=pregeno_genozdbid) where exists (select 'x' from pre_geno where geno_zdb_id=pregeno_genozdbid) and geno_display_name like 'ZDB-GENO-%';

update genotype set geno_nickname=(select pregeno_nick_name from pre_geno where geno_zdb_id=pregeno_genozdbid) where exists (select 'x' from pre_geno where geno_zdb_id=pregeno_genozdbid) and geno_nickname like 'ZDB-GENO-%';

update genotype set geno_handle=(select pregeno_handle from pre_geno where geno_zdb_id=pregeno_genozdbid) where exists (select 'x' from pre_geno where geno_zdb_id=pregeno_genozdbid) and geno_handle like 'ZDB-GENO-%';

unload to 'pre_geno.unl' select * from genotype where geno_zdb_id like 'ZDB-GENO-120307%';

drop table pre_gene;
drop table pre_feature;
drop table pre_feature_marker_relationship;
drop table pre_geno;
drop table pre_geno_ftr_relationship;
drop table pre_db_link;

                                 
--rollback work;

commit work;

--execute function regen_names();

