--liquibase formatted sql
--changeset pkalita:DLOAD-353

-- based on loadSangerDataApril2016.sql

create temp table sanger_input_known (
  sanger_input_feature_abbrev varchar(255),
  sanger_input_gene_zdb_id varchar (50))
with no log;

insert into sanger_input_known values ('sa13694', 'ZDB-GENE-071218-5');
insert into sanger_input_known values ('sa14051', 'ZDB-GENE-060404-6');
insert into sanger_input_known values ('sa16713', 'ZDB-GENE-071218-5');
insert into sanger_input_known values ('sa17187', 'ZDB-GENE-090831-2');
insert into sanger_input_known values ('sa20256', 'ZDB-GENE-041210-175');
insert into sanger_input_known values ('sa22355', 'ZDB-GENE-071218-5');
insert into sanger_input_known values ('sa22356', 'ZDB-GENE-071218-5');
insert into sanger_input_known values ('sa22357', 'ZDB-GENE-071218-5');
insert into sanger_input_known values ('sa23651', 'ZDB-GENE-040724-50');

update sanger_input_known
	   set sanger_input_gene_zdb_id =  (select zrepld_new_zdb_id
                                 from zdb_replaced_data
                                where zrepld_old_zdb_id = sanger_input_gene_zdb_id)
         where sanger_input_gene_zdb_id in (select zrepld_old_zdb_id
                                  from zdb_replaced_data);

delete from sanger_input_known where sanger_input_feature_abbrev in (select feature_abbrev from feature);

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) select distinct sanger_input_gene_zdb_id, 'ZDB-PUB-130425-4' from sanger_input_known where sanger_input_gene_zdb_id not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-130425-4');

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) select distinct feature_zdb_id, 'ZDB-PUB-130425-4' from sanger_input_known, feature where feature_abbrev=sanger_input_feature_abbrev and feature_zdb_id not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-130425-4');

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id) select fmrel_zdb_id , 'ZDB-PUB-130425-4' from feature, feature_marker_relationship where feature_zdb_id=fmrel_ftr_zdb_id and feature_abbrev in (select sanger_input_feature_abbrev from sanger_input_known) and fmrel_zdb_id not in (select recattrib_data_zdb_id from record_attribution where recattrib_source_zdb_id='ZDB-PUB-130425-4');

 

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
  select distinct sanger_input_feature_abbrev,
                  sanger_input_gene_zdb_id,
                  'ZDB-LAB-050412-2',
                  'adult males',
                  'ENU',
                  SUBSTR(sanger_input_feature_abbrev,3),
                  fp_pk_id
    from sanger_input_known, feature_prefix
     where fp_prefix = "sa"
     and sanger_input_feature_abbrev like 'sa%'
     and sanger_input_feature_abbrev not in (select feature_abbrev from feature);




alter table pre_feature add preftr_feature_zdb_id varchar(50);

update pre_feature set preftr_feature_zdb_id = get_id('ALT');




insert into zdb_active_data select preftr_feature_zdb_id from pre_feature;



insert into feature (
    feature_zdb_id,
    feature_name,
    feature_abbrev,
    feature_type,
    feature_lab_prefix_id,
    feature_line_number
)
select  distinct preftr_feature_zdb_id,
        preftr_feature_abbrev,
        preftr_feature_abbrev,
        'POINT_MUTATION',
        preftr_lab_prefix_id,
        preftr_line_number
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



---------------------------
--       LOCATIONS       --
---------------------------
create temp table sanger_locations (
  zdb_id varchar(50),
  ftrAbbrev varchar(50),
  ftrAssembly varchar(10),
  ftrChrom varchar(2),
  locStart integer
) with no log;

insert into sanger_locations values (get_id('SFCL'), 'sa13694', 'GRCz10', '13', '37103472');
insert into sanger_locations values (get_id('SFCL'), 'sa14051', 'GRCz10', '21', '8377341');
insert into sanger_locations values (get_id('SFCL'), 'sa16713', 'GRCz10', '13', '37078781');
insert into sanger_locations values (get_id('SFCL'), 'sa17187', 'GRCz10', '9', '44840938');
insert into sanger_locations values (get_id('SFCL'), 'sa20256', 'GRCz10', '4', '18491430');
insert into sanger_locations values (get_id('SFCL'), 'sa22355', 'GRCz10', '13', '37010554');
insert into sanger_locations values (get_id('SFCL'), 'sa22356', 'GRCz10', '13', '37085617');
insert into sanger_locations values (get_id('SFCL'), 'sa22357', 'GRCz10', '13', '37107958');
insert into sanger_locations values (get_id('SFCL'), 'sa23651', 'GRCz10', '20', '13764522');

insert into zdb_active_data
 select zdb_id from sanger_locations;

insert into sequence_feature_chromosome_location (sfcl_zdb_id, sfcl_feature_zdb_id,sfcl_start_position,sfcl_end_position,sfcl_assembly,sfcl_chromosome)
select zdb_id, feature_zdb_id, locStart,locStart,ftrAssembly,ftrChrom
from sanger_locations,feature where ftrChrom not like 'Zv%'  and ftrAbbrev=feature_abbrev;

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select zdb_id,'ZDB-PUB-130425-4' ,'standard' from sanger_locations;



---------------------------
--   MUTATION DETAILS    --
---------------------------

create temp table ftrMutDets (ftr varchar(50), ref1 varchar(50), ref2 varchar(50))
with no log;

insert into ftrMutDets values ('sa13694', 'G', 'A');
insert into ftrMutDets values ('sa14051', 'T', 'A');
insert into ftrMutDets values ('sa16713', 'A', 'T');
insert into ftrMutDets values ('sa17187', 'G', 'A');
insert into ftrMutDets values ('sa20256', 'T', 'A');
insert into ftrMutDets values ('sa22355', 'C', 'T');
insert into ftrMutDets values ('sa22356', 'C', 'T');
insert into ftrMutDets values ('sa22357', 'A', 'T');
insert into ftrMutDets values ('sa23651', 'G', 'A');

create temp table ftrMutDetsnew (ftr varchar(50), ref1 varchar(50), ref2 varchar(10),mutDisplay varchar(10), fdmd_zdb_id varchar(50)) ;

insert into ftrMutDetsnew (ftr,ref1,ref2,mutDisplay, fdmd_zdb_id)
select ftr,ref1,ref2,trim(ref1)||">"||trim(ref2), get_id('FDMD')
       from ftrMutDets
       where ref1!=''
       and ref2!='';

update ftrMutDetsnew
       set ref1=(select mdcv_term_zdb_id
       	   	 from mutation_detail_controlled_vocabulary
		 where mdcv_term_display_name=mutDisplay);

insert into zdb_Active_data
 select fdmd_zdb_id from ftrMutDetsnew;

 select fdmd_zdb_id, ref1, feature_zdb_id
   from ftrMutDetsnew, feature
 where feature_abbrev = ftr
into temp tmp_load;

insert into tmp_load (fdmd_zdb_id, ref1, feature_zdb_id)
  select distinct fdmd_zdb_id, ref1, dalias_data_zdb_id
   from data_alias, ftrMutDetsnew
 where dalias_alias = ftr;

delete  from tmp_load where feature_zdb_id in (select
fdmd_feature_zdb_id from feature_dna_mutation_detail);

insert into feature_dna_mutation_Detail (fdmd_zdb_id, fdmd_feature_zdb_id, fdmd_dna_mutation_term_Zdb_id)
 select distinct fdmd_zdb_id, feature_zdb_id, ref1
  from tmp_load
 where feature_zdb_id is not null
;

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id) select distinct fdmd_zdb_id,'ZDB-PUB-130425-4' from tmp_load where feature_zdb_id is not null;


create temp table ftrConsequence (ftr varchar(50), cons varchar(50)) with no log;

insert into ftrConsequence values ('sa13694', 'splice_site_variant');
insert into ftrConsequence values ('sa14051', 'stop_gained');
insert into ftrConsequence values ('sa16713', 'stop_gained');
insert into ftrConsequence values ('sa17187', 'stop_gained');
insert into ftrConsequence values ('sa20256', 'stop_gained');
insert into ftrConsequence values ('sa22355', 'stop_gained');
insert into ftrConsequence values ('sa22356', 'stop_gained');
insert into ftrConsequence values ('sa22357', 'stop_gained');
insert into ftrConsequence values ('sa23651', 'stop_gained');

create temp table ftCq (ftmd varchar(50),featureAbb varchar(50),featurezdb varchar(50),cq1 varchar(50),cqzdb varchar(50)) with no log;

insert into ftCq
       select distinct ftr,ftr,ftr,cons,cons
        from ftrConsequence
	where cons is not null
	 and ftr in (Select feature_abbrev from feature);

update ftCq set ftmd =  get_id('FTMD');

insert into zdb_active_data
  select ftmd from ftCq;

update ftCq
 set featurezdb=(select feature_zdb_id
     			from feature
			where featureAbb=feature_abbrev);

update ftCq
 set cqzdb=(select mdcv_term_zdb_id
     		   from mutation_detail_controlled_vocabulary
     		   inner join term on mutation_detail_controlled_vocabulary.mdcv_term_zdb_id = term.term_zdb_id
		       where cq1=term_name);

insert into feature_transcript_mutation_detail (ftmd_zdb_id,ftmd_transcript_consequence_term_zdb_id,ftmd_feature_zdb_id)
  select distinct ftmd,cqzdb,featurezdb
   from ftCq
   where featurezdb not in (select ftmd_feature_zdb_id
   	 	    	   	   from feature_transcript_mutation_detail)
   and cqzdb is not null;

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id)
  select distinct ftmd,'ZDB-PUB-130425-4'
   from ftCq;

drop table pre_feature;
drop table pre_feature_marker_relationship;
drop table pre_db_link;
drop table sanger_input_known;
drop table sanger_locations;