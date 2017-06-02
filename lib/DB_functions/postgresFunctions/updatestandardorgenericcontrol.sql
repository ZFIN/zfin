create or replace function updatestandardorgenericcontrol(vExpZdbId varchar(50))
returns boolean as $true$ 
  
  declare stdGc boolean := 'f';
  begin

  if exists (Select 'x' from experiment
                      where exp_zdb_id = vExpZdbId
                      and exp_name in ('_Standard', '_Generic_control') 
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
		      and term_ont_id in ('ZECO:0000103','ZECO:0000102')
		      and expcond_exp_zdb_id = vExpZdbId)
  
  then 
      stdGc = 't';
  end if;

  return stdGc; 

  end 
$true$ LANGUAGE plpgsql
