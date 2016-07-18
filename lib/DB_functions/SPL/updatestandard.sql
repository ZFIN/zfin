
create function updatestandard(vExpZdbId like fish_experiment.genox_exp_zdb_id)
  returning boolean;

  define std boolean; 
  let std = 'f';
 
  if exists (Select 'x' from experiment
                      where exp_zdb_id = vExpZdbId
                      and exp_name in ('_Standard') 
               union
	       select 't'
	       	      from experiment
		      where not exists (Select 'x' from experiment_condition
		      	    	       	       where expcond_exp_zdb_id = exp_zdb_id)
                      and exp_Zdb_id = vExpZdbId
   	       union
	       select 't'
	       	      from experiment_condition, term
		      where term_zdb_id = expcond_zeco_term_zdb_id
		      and term_ont_id = "ZECO:0000103"
		      and expcond_exp_zdb_id = vExpZdbId)
  
  then 
       let std = 't';
  end if;

       return std;

end function;
