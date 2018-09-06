--liquibase formatted sql
--changeset pm:DLOAD-559a

-- based on loadSangerDataApril2016.sql

---------------------------
--   MUTATION DETAILS    --
---------------------------


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




drop table pre_feature;
drop table pre_feature_marker_relationship;
drop table pre_db_link;
drop table sanger_input_known;
drop table sanger_locations;