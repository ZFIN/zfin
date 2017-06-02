create or replace function p_feature_stats_check_constraints (vPkID int8, 
       		 vFeatID varchar(50),
		 vSuperTermID varchar(50),
		 vSubTermID varchar(50),
		 vFigID varchar(50),
		 vPubID varchar(50),
		 vXpatResID varchar(50))
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
