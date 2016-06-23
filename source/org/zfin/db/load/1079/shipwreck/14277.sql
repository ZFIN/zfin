--liquibase formatted sql 
--changeset sierra:14277



create temp table tmp_ids_to_remove (id varchar(50))
with no log;

insert into tmp_ids_to_remove (id)
 select geno_zdb_id
   from genotype
 where exists (select 'x' from genotype_Feature, record_attribution
       	      	      	  where genofeat_geno_zdb_id = geno_zdb_id
			  and genofeat_feature_zdb_id = recattrib_data_zdb_id
			  and recattrib_source_zdb_id in ('ZDB-PUB-120207-1','ZDB-PUB-150729-10'))
 and not exists (select 'x' from fish_Experiment, fish
     	 		where geno_zdb_id = fish_genotype_zdb_id
			and genox_fish_zdb_id = fish_zdb_id)
 and not exists (select 'x' from record_attribution
     	 		where recattrib_datA_zdb_id = geno_zdb_id
			and recattrib_source_zdb_id not in ('ZDB-PUB-120207-1','ZDB-PUB-150729-10'));


insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id)
  select id, 'ZDB-PUB-160615-7'
   from tmp_ids_to_remove;

delete from zdb_active_data
 where zactvd_zdb_id in (select id from tmp_ids_to_remove);
     	 		 

