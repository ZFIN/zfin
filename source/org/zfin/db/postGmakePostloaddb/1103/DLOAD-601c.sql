--liquibase formatted sql
--changeset pm:DLOAD-601c

create temp table ftrMutDetsnew (ftr varchar(50), ref1 varchar(50), ref2 varchar(10),mutDisplay varchar(10), fdmd_zdb_id varchar(50)) ;

insert into ftrMutDetsnew (ftr,ref1,ref2,mutDisplay, fdmd_zdb_id)
select ftr,ref1,ref2,trim(ref1)||'>'||trim(ref2), get_id('FDMD')
       from ftrmutdets, feature
       where ref1!=''
       and ref2!='' and feature_abbrev=ftr and feature_zdb_id like 'ZDB-ALT-1812%' and feature_abbrev like 'sa%';

update ftrMutDetsnew
       set ref1=(select mdcv_term_zdb_id
       	   	 from mutation_detail_controlled_vocabulary
		 where mdcv_term_display_name=mutDisplay);

insert into zdb_Active_data
 select fdmd_zdb_id from ftrMutDetsnew;


--drop table tmp_load;
create temp table tmp_load (ftrtemp varchar(50), ref1temp varchar(50), fdmd_zdb_idtemp varchar(50)) ;

 insert into tmp_load(ftrtemp, ref1temp , fdmd_zdb_idtemp)
 select fdmd_zdb_id, ref1, feature_zdb_id
   from ftrMutDetsnew, feature
 where feature_abbrev = ftr;


delete  from tmp_load where fdmd_zdb_idtemp  in (select
fdmd_feature_zdb_id from feature_dna_mutation_detail);

insert into feature_dna_mutation_Detail (fdmd_zdb_id, fdmd_feature_zdb_id, fdmd_dna_mutation_term_Zdb_id)
 select distinct ftrtemp, fdmd_zdb_idtemp, ref1temp
  from tmp_load
 where fdmd_zdb_idtemp is not null
;

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id) select distinct ftrtemp,'ZDB-PUB-130425-4' from tmp_load where fdmd_zdb_idtemp is not null;

