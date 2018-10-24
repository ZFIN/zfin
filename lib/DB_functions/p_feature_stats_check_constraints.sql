create or replace function p_feature_stats_check_constraints (vPkID int8, 
       		 vFeatID text,
		 vSuperTermID text,
		 vSubTermID text,
		 vFigID text,
		 vPubID text,
		 vXpatResID text)
returns void as $$
declare vOk int;
begin
  if vFeatID is not null
     and vSuperTermID is not null
     and vSubTermID is not null
     and vFigID is not null
     and vPubID is not null
     and vXpatResID is not null
     and not exists (select 'x' from feature_stats
     	     	    	    where vPkID = fstat_pk_id)
    then
	 vOk = 1;
    
   end if;

vOk = 0;
if vOk = 0 then
  raise exception 'FAIL!!: feature_stats data is null or duplicate';
  end if ;

end
$$ LANGUAGE plpgsql
