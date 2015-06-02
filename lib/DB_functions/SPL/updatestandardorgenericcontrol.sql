create function updatestandardorgenericcontrol(vGenoxZdbId like fish_experiment.genox_zdb_id)
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