create procedure p_feature_stats_check_constraints (vPkID int8, 
       		 vFeatID varchar(50),
		 vSuperTermID varchar(50),
		 vSubTermID varchar(50),
		 vFigID varchar(50),
		 vPubID varchar(50),
		 vXpatResID varchar(50))

define vOk int;

  if vFeatID is not null
     and vSuperTermID is not null
     and vSubTermID is not null
     and vFigID is not null
     and vPubID is not null
     and vXpatResID is not null
     and not exists (select 'x' from feature_stats
     	     	    	    where vPkID = fstat_pk_id)
    then
	let vOk = 1;
    
   end if;

let vOk = 0;
if vOk = 0 then
  raise exception -746,0,'FAIL!!: feature_stats data is null or duplicate';
  end if ;


end procedure;

--to test:
--gmake with Makefile addition
--or % dbaccess nagdb p_feature_stats_check_constraints.sql 
--echo "execute procedure p_feature_stats_check_constraints (2,'feat','super','sub','fig','pub','res')" | dbaccess nagdb
