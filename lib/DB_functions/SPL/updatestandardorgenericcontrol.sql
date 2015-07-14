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
	       	      from experiment_condition a, condition_Data_type
		      where a.expcond_cdt_zdb_id = cdt_zdb_id
		      and a.expcond_exp_Zdb_id = vExpZdbId
		      and cdt_group in ('TALEN','CRISPR','morpholino')
		      and not exists (select 'x' from experiment_condition b, 
		      	      	     	     	      condition_data_type c
		      	      where b.expcond_cdt_zdb_id = c.cdt_zdb_id
			      and c.cdt_group not in ('TALEN','CRISPR','morpholino')
			      and b.expcond_exp_zdb_id = vExpZdbId
			      and a.expcond_zdb_id != b.expcond_zdb_id)
			      );
  if (stdGc = 't') 
then 
return stdGc; 
  else return fal;
 end if;

end function;