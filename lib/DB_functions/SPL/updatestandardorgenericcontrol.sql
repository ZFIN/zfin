create function updatestandardorgenericcontrol(vExpZdbId like fish_experiment.genox_exp_zdb_id)
  returning boolean;
  define fal boolean;
  
  define stdGc boolean;  
  let fal = 'f';
  let stdGc = (select 't'
                      from experiment
                      where exp_zdb_id = vExpZdbId
                      and exp_name in ('_Standard') 
               union
	       select 't'
	       	      from experiment
		      where not exists (Select 'x' from experiment_condition
		      	    	       	       where expcond_exp_zdb_id = exp_zdb_id));

  if (stdGc = 't') 
then 
return stdGc; 
  else return fal;
 end if;

end function;