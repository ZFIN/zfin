begin work ;

set triggers for atomic_phenotype disabled ;

update atomic_phenotype
  set apato_start_stg_zdb_id= (select 
stg_zdb_id
                                from stage
                                where stg_name = 'Unknown')
  where apato_Start_stg_zdb_id is null;

update atomic_phenotype
  set apato_end_stg_zdb_id= (select                        
stg_zdb_id
                                from stage
                                where stg_name = 'Unknown') 
  where apato_end_stg_zdb_id is null;

set triggers for atomic_phenotype enabled; 
--rollback work;
commit work ;
