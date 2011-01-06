create procedure p_update_unspecified_alleles (vMarkerZdbId varchar(50),
       		 			       vFeatureZdbId varchar(50))

    define vUnallele like feature.feature_abbrev;
    define vUnspecified like feature.feature_unspecified;
    define vUnExists int;					    
   
   let vUnAllele = (select feature_abbrev from feature
        	  	  where feature_zdb_id = vFeatureZdbId);

   let vUnspecified = (select feature_unspecified from feature
        	  	  where feature_zdb_id = vFeatureZdbId);

  if (vUnspecified)

  then

        let vUnExists = (Select count(*) 
    	          	     from feature, feature_marker_relationship
			     where feature_zdb_id = fmrel_ftr_zdb_id
                     	     and feature_unspecified = 't'
			     and fmrel_type = 'is allele of'
		     	     and fmrel_mrkr_zdb_id = vMarkerZdbId);

        if (vUnExists > 1)

    	  then raise exception -746,0,"FAIL!: unspecified allele already exists for gene. p_update_unspecified_alleles.";
  	
        end if ;

  end if ;

end procedure ;