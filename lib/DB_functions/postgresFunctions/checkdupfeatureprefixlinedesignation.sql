create or replace function checkdupfeatureprefixlinedesignation (vFeaturePrefix int8, vFeatureLineDesignation varchar(60))
returns void as $$
      declare vOk int;
      begin
	   vOk = (select count(*) from feature 
	     	     	     	 where feature_lab_prefix_id = vFeaturePrefix
	     	     	     	 and feature_line_number = vFeatureLineDesignation
				 and feature_lab_prefix_id is not null
				 and feature_line_number is not null);

	  if (vOk > 1 )
	  
	  then 
	      raise exception 'FAIL!: feature prefix and line design already exist!';
	  
          end if ;
     end
$$ LANGUAGE plpgsql
