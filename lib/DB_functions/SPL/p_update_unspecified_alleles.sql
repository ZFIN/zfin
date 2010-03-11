create procedure p_update_unspecified_alleles (vMarkerZdbId varchar(50),
       		 			       vFeatureZdbId varchar(50))

    define vFeatureName like feature.feature_name;
    define vFeatureAbbrev like feature.feature_abbrev;
    define vUnExists int ;
    define vUnAllele like feature.feature_name;
   
  let vUnAllele = (select feature_name from feature
        	  	  where feature_zdb_id = vFeatureZdbId);

  if (vUnAllele like 'un_%')

  then

        let vUnExists = (Select count(*) 
    	          	     from feature, feature_marker_relationship
			     where feature_zdb_id = fmrel_ftr_zdb_id
                     	     and feature_name like 'unspecified_%'
			     and fmrel_type = 'is allele of'
		     	     and fmrel_mrkr_zdb_id = vMarkerZdbId);

        if (vUnExists > 1)

    	  then raise exception -746,0,"FAIL!: an un_ allele already exists for the gene.  Please use it instead.";
  	
        end if ;

  end if ;


end procedure ;
