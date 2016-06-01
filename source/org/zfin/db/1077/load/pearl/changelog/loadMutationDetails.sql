--liquibase formatted sql
--changeset pm:loadMutationDetails

create temp table ftrMutDetsnew (ftr varchar(50), ref1 varchar(50), ref2 varchar(10),mutDisplay varchar(10), fdmd_zdb_id varchar(50)) with no log;


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

select first 10 * from ftrMutDetsnew;


 select fdmd_zdb_id, ref1, feature_zdb_id
   from ftrMutDetsnew, feature
 where feature_abbrev = ftr
into temp tmp_load;

insert into tmp_load (fdmd_zdb_id, ref1, feature_zdb_id)
  select distinct fdmd_zdb_id, ref1, dalias_data_zdb_id
   from data_alias, ftrMutDetsnew
 where dalias_alias = ftr;

insert into feature_dna_mutation_Detail (fdmd_zdb_id, fdmd_feature_zdb_id, fdmd_dna_mutation_term_Zdb_id)
 select distinct fdmd_zdb_id, feature_zdb_id, ref1
  from tmp_load
 where feature_zdb_id is not null
;



--update feature 
--       set feature_dna_mutation_term_zdb_id=(select ref1 
 --      	   					    from ftrMutDetsnew 
--						    where feature_abbrev=ftr) 
--      where exists (select  'x' from ftrMutDetsnew where feature_abbrev=ftr);
