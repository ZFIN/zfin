create or replace function p_update_fmrel_genotype_names (vNewZDBid text,
					vOldZdbId text)
returns void as $$

	declare vDataType varchar(30) ;
	 vGenotypeFtrCount integer ;	
	 vFmrelCount integer; 
	 vNewFtrAbbrev  feature.feature_abbrev%TYPE;
	 vOldFtrAbbrev  feature.feature_abbrev%TYPE;
	 vNewMrkrAbbrev  marker.mrkr_abbrev%TYPE ;
	 vOldMrkrAbbrev  marker.mrkr_abbrev%TYPE ;

    begin
	if get_obj_type(vOldZDBid) = 'ALT'
	then 
  	  vDataType := 'feature' ;

	else
	  vDataType := 'marker' ;

	end if ; -- end if get_obj_type = 'ALT'

	if vDataType = "feature"
	then
	  vGenotypeFtrCount := (Select count(*) 
			  	     from genotype_feature
			  	     where genofeat_feature_zdb_id = vOldZDBid);


	  if vGenotypeFtrCount > 0
	  	then 

	  	vNewFtrAbbrev := (select feature_abbrev
				from feature
				where feature_zdb_id = vNewZdbId);

	 	vOldFtrAbbrev := (select feature_abbrev
				 from feature
				 where feature_zdb_id = vOldZdbId) ;

		update genotype 
		  set (geno_display_name, geno_handle) = 
			(replace(geno_display_name,vOldFtrAbbrev,
					vNewFtrAbbrev),
			replace(geno_handle,vOldFtrAbbrev,vNewFtrAbbrev))
		  where exists (Select 'x'
				  from genotype_feature,feature_marker_relationship
				  where genofeat_geno_zdb_id = geno_zdb_id
				  and genofeat_feature_zdb_id = vNewZDBid
				  and genofeat_feature_zdb_id = fmrel_ftr_zdb_id)
                 ;

	  end if ; -- end if vGenotypeFtrCount > 0

 	elsif vDataType = 'marker'
	then

	  vNewMrkrAbbrev := (select mrkr_abbrev
				from marker
				where mrkr_zdb_id = vNewZdbId);

	  vOldMrkrAbbrev := (select mrkr_abbrev
				 from marker
				 where mrkr_zdb_id = vOldZdbId) ;

	  vFmrelCount := (select count(*)
                                from feature_marker_relationship, 
                                     genotype_feature
                                where fmrel_ftr_zdb_id = 
					genofeat_feature_zdb_id
				and fmrel_mrkr_zdb_id = vNewZdbId
                               ); 


	  if vFmrelCount > 0

	  then 
		update genotype 
		  set (geno_display_name, geno_handle) =
			(replace(geno_display_name,vOldMrkrAbbrev,
					vNewMrkrAbbrev),
			replace(geno_handle,vOldMrkrAbbrev,vNewMrkrAbbrev))
		  where exists (select 'x'
				  from feature_marker_relationship,
					genotype_feature
				  where fmrel_ftr_zdb_id = 
						genofeat_feature_zdb_id
				   and  fmrel_mrkr_zdb_id = vNewZdbId
				   and geno_zdb_id = genofeat_geno_zdb_id);

	  end if ;

	end if;  -- end if vDataType = 'marker'

end
$$ LANGUAGE plpgsql
