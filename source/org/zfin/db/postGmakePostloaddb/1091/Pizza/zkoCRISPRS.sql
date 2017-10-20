--liquibase formatted sql
--changeset pm:zkoCRISPRS






---creating temp table to load all of the input data form sanger
create temp table sanger_pre_input_known (
     sanger_preinput_feature_abbrev varchar(255),
     sanger_preinput_gene_zdb_id varchar (50),
     sanger_preinput_background varchar(50),
     sanger_preinput_line_number varchar(70),
     mutationtype varchar(25),
     accession varchar(10)
) with no log;

insert into sanger_pre_input_known select feature_abbrev,tgtgeneid,tgtgenesymbol,feature_abbrev,mutationtype,pageURL from tmp_feature where tgtgeneid!='';


create temp table sanger_input_known (
     sanger_input_feature_abbrev varchar(255),
     sanger_input_gene_zdb_id varchar (50),
     sanger_input_background varchar(50),
     sanger_input_line_number varchar(70),
     mutationtype varchar(25),
     accession varchar(10)
) with no log;

insert into sanger_input_known select distinct * from sanger_pre_input_known;




--updating temp table with ZFIN gene id's based on matches.








---creating temp table to create new ZFIN unnamed genes for those with no matches

create table pre_gene (
        gene_zdb_id varchar(50),
        dblink_id varchar(50),
        gene_abbrev varchar(255),
        gene_name varchar(255),
        feature_abbrev varchar(50),
        mutationtype varchar(25),
        accession varchar(10));

insert into pre_gene select tgtgeneid,tgtgeneid,  "unm_"||feature_abbrev, "un-named "||feature_abbrev,feature_abbrev,mutationtype,pageURL  from tmp_feature where tgtgeneid='';



update pre_gene set gene_zdb_id=get_id('GENE');


insert into zdb_active_data select gene_zdb_id from pre_gene;



insert into marker(mrkr_zdb_id, mrkr_abbrev, mrkr_name, mrkr_owner,mrkr_type) select distinct gene_zdb_id,gene_abbrev,gene_name,"ZDB-PERS-981201-7","GENE" from pre_gene;

/*
insert
into
   marker_history
   (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, mhist_date, mhist_mrkr_name_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date)
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
*/

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) select distinct gene_zdb_id, 'ZDB-PUB-171002-4' from pre_gene;
create table pre_feature (
        preftr_feature_abbrev varchar(255),
        preftr_gene_zdb_id varchar (50),
        preftr_data_source varchar(50),
        preftr_mutagee varchar(20),
        preftr_mutagen varchar(20),
        preftr_line_number varchar(70),
        preftr_lab_prefix_id int8,
        preftr_type varchar(25),
        preftr_accession varchar(10)
);

-- if the feature is not in ZFIN and no affected gene and only sa's
insert into pre_feature (
      preftr_feature_abbrev,
      preftr_gene_zdb_id,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_line_number,
      preftr_lab_prefix_id,
      preftr_type,preftr_accession
      )
  select distinct sanger_input_feature_abbrev,
                  sanger_input_gene_zdb_id,
                  'ZDB-LAB-130226-1',
                  'embryos',
                  'CRISPR',
                  SUBSTRING(sanger_input_line_number from 4),
                  fp_pk_id,
                  mutationtype,accession
    from sanger_input_known, feature_prefix
     where fp_prefix = "zko"

     and sanger_input_feature_abbrev not in (select feature_abbrev from feature);
insert into pre_feature (
      preftr_feature_abbrev,
      preftr_gene_zdb_id,
      preftr_data_source,
      preftr_mutagee,
      preftr_mutagen,
      preftr_line_number,
      preftr_lab_prefix_id,
      preftr_type,preftr_accession
      )
  select distinct feature_abbrev,
                  gene_zdb_id,
                  'ZDB-LAB-070815-1',
                  'embryos',
                  'CRISPR',
                  SUBSTRING(feature_abbrev from 4),
                  fp_pk_id,mutationtype,accession
    from pre_gene, feature_prefix
     where fp_prefix = "zko";





alter table pre_feature add preftr_feature_zdb_id varchar(50);

update pre_feature set preftr_feature_zdb_id = get_id('ALT');




insert into zdb_active_data select preftr_feature_zdb_id from pre_feature;



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
preftr_type,
        preftr_lab_prefix_id,
        preftr_line_number
 from pre_feature;



-- load record_attribution table
insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
)
select  preftr_feature_zdb_id,
        'ZDB-PUB-171002-4'
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
  select preftr_feature_zdb_id, preftr_gene_zdb_id, 'is allele of'
    from pre_feature;





alter table pre_feature_marker_relationship add prefmrel_zdb_id varchar(50);

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
        'ZDB-PUB-171002-4'
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
  select distinct preftr_feature_zdb_id, preftr_accession, preftr_accession, fdbcont_zdb_id
    from pre_feature, foreign_db, foreign_db_contains
   where fdbcont_fdb_db_id = fdb_db_pk_id
     and fdb_db_name = 'CZRC';






alter table pre_db_link add predblink_dblink_zdb_id varchar(50);

update pre_db_link set predblink_dblink_zdb_id = get_id('DBLINK');


insert into zdb_active_data select predblink_dblink_zdb_id from pre_db_link;



insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id)
  select predblink_data_zdb_id, predblink_acc_num, predblink_dblink_zdb_id, predblink_acc_num_display, predblink_fdbcont_zdb_id
    from pre_db_link;




insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select predblink_dblink_zdb_id,'ZDB-PUB-171002-4' from pre_db_link;



drop table pre_gene;
drop table pre_feature;
drop table pre_feature_marker_relationship;


drop table pre_db_link;


--rollback work;

commit work;

--execute function regen_names();



