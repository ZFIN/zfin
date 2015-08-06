create function updatestandardorgenericcontrol(vExpZdbId like fish_experiment.genox_exp_zdb_id)
  returning boolean;
  
  define stdGc boolean;  
  let stdGc = 'f';


 if exists (Select 'x' from experiment
                      where exp_zdb_id = vExpZdbId
                      and exp_name in ('_Standard', '_Generic_control') 
               union
	       select 't'
	       	      from experiment
		      where not exists (Select 'x' from experiment_condition
		      	    	       	       where expcond_exp_zdb_id = exp_zdb_id))
  
then 
     let stdGc = 't';
end if;

return stdGc; 

end function;