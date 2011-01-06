create procedure checkDupFeaturePrefixLineDesignation (vFeaturePrefix int8, vFeatureLineDesignation varchar(60))

      define vOk int;
	  let vOk = (select count(*) from feature 
	     	     	     	 where feature_lab_prefix_id = vFeaturePrefix
	     	     	     	 and feature_line_number = vFeatureLineDesignation
				 and feature_lab_prefix_id is not null
				 and feature_line_number is not null);

	  if (vOk > 1 )
	  
	  then 
	      raise exception -746,0,"FAIL!: feature prefix and line design already exist!";
	  
          end if ;
      	      
end procedure;
