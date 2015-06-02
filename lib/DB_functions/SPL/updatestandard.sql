create function "informix".updatestandard(vGenoxZdbId like fish_experiment.genox_zdb_id)
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