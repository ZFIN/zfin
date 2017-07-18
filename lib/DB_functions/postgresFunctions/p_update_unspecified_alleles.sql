create or replace function p_update_unspecified_alleles (vMarkerZdbId text,
       		 			       vFeatureZdbId text)
returns void as $$
    declare vUnallele feature.feature_abbrev%TYPE;
     vUnspecified feature.feature_unspecified%TYPE;
     vUnExists int;					    
   begin 
   vUnAllele := (select feature_abbrev from feature
        	  	  where feature_zdb_id = vFeatureZdbId);

   vUnspecified := (select feature_unspecified from feature
        	  	  where feature_zdb_id = vFeatureZdbId);

  if (vUnspecified)

  then

        vUnExists := (Select count(*) 
    	          	     from feature, feature_marker_relationship
			     where feature_zdb_id = fmrel_ftr_zdb_id
                     	     and feature_unspecified = 't'
			     and fmrel_type = 'is allele of'
		     	     and fmrel_mrkr_zdb_id = vMarkerZdbId);

        if (vUnExists > 1)

    	  then raise exception 'FAIL!: unspecified allele already exists for gene. p_update_unspecified_alleles.';
  	
        end if ;

  end if ;
end
$$ LANGUAGE plpgsql;
