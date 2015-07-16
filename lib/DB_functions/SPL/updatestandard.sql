create function updatestandard(vExpZdbId like fish_experiment.genox_exp_zdb_id)
  returning boolean;

  define fal boolean;
  define std boolean; 
 
  let fal = 'f';
  let std = (select 't'
                      from experiment
                      where exp_zdb_id = vExpZdbId
                      and exp_name in ('_Standard') 
               union
	       select 't'
	       	      from experiment
		      where not exists (Select 'x' from experiment_condition
		      	    	       	       where expcond_exp_zdb_id = exp_zdb_id));
  if (std = 't') 
  then 
       return std; 
  else return fal;
 end if;

end function;