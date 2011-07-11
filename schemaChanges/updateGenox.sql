begin work ;

alter table genotype_experiment
  add (genox_is_standard boolean default 'f');

alter table genotype_experiment
  add (genox_is_std_or_generic_control boolean default 'f');

update genotype_experiment
  set genox_is_standard = 't' where exists (Select 'x' from experiment
      			where exp_zdb_id = genox_exp_zdb_id 
			and exp_name = '_Standard');

update genotype_experiment
  set genox_is_std_or_generic_control = 't' where exists (Select 'x' from experiment
      			where exp_zdb_id = genox_exp_zdb_id 
			and exp_name = '_Standard'
			or exp_name = '_Generic-control');

create function updateStandardOrGenericControl(vGenoxZdbId like genotype_experiment.genox_zdb_id)
  returning boolean;
  define fal boolean;
  
  define stdGc boolean;  
  let fal = 'f';
  let stdGc = (select 't'
                      from experiment
                      where exp_zdb_id = vGenoxZdbId
                      and exp_name in ('_Standard','_Generic-control'));
  if (stdGc = 't') 
then 
return stdGc; 
  else return fal;
 end if;

end function;

create function updateStandard(vGenoxZdbId like genotype_experiment.genox_zdb_id)
  returning boolean;

  define fal boolean;
  define std boolean; 
 
  let fal = 'f';
  let std = (select 't'
                      from experiment
                      where exp_zdb_id = vGenoxZdbId
                      and exp_name in ('_Standard'));
  if (std = 't') 
  then 
       return std; 
  else return fal;
 end if;

end function;  


create trigger genox_exp_insert_trigger
  insert on genotype_experiment
  referencing new as newGenox
  for each row (execute function updateStandard(newGenox.genox_exp_zdb_id) 
      	       			 into genotype_Experiment.genox_is_standard,
      	        execute function updateStandardOrGenericControl(newGenox.genox_exp_zdb_id) 
				 into genotype_experiment.genox_is_std_or_generic_control)
		; 

create trigger genox_exp_update_trigger
  update of genox_exp_zdb_id on genotype_experiment
  referencing new as newGenox
  for each row (execute function updateStandard(newGenox.genox_exp_zdb_id) 
      	       			 into genotype_Experiment.genox_is_standard,
      	        execute function updateStandardOrGenericControl(newGenox.genox_exp_zdb_id) 
				 into genotype_experiment.genox_is_std_or_generic_control)
		; 


insert into zdb_Active_data
  values ('1');



insert into genotype_experiment (genox_zdb_id, genox_geno_zdb_id, genox_exp_zdb_id)
  values( '1','ZDB-GENO-070117-10','ZDB-EXP-041102-1') 
   ;

select * from genotype_experiment
 where genox_zdb_id = '1';

commit work ;

--rollback work ;