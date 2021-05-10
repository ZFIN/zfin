
create or replace function updatestandard(vExpZdbId text)
  returns boolean as $true$

declare std boolean := 'f';
 
begin
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
		      and term_ont_id = 'ZECO:0000103'
		      and expcond_exp_zdb_id = vExpZdbId)
  
  then 
       std := 't';
  end if;

       return std;
end
$true$ LANGUAGE plpgsql;
