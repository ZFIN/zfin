create procedure p_update_fmrel_genotype_names (vNewZDBid varchar(50),
					vOldZdbId varchar(50))


	define vDataType varchar(30) ;
	define vGenotypeFtrCount integer ;	
	define vFmrelCount integer; 
	define vNewFtrAbbrev like feature.feature_abbrev;
	define vOldFtrAbbrev like feature.feature_abbrev;
	define vNewMrkrAbbrev like marker.mrkr_abbrev ;
	define vOldMrkrAbbrev like marker.mrkr_abbrev ;

	if get_obj_type(vOldZDBid) = 'ALT'
	then 
  	  let vDataType = 'feature' ;

	elif get_obj_type(vOldZDBid) != 'ALT'
        then
	  let vDataType = 'marker' ;

	end if ; -- end if get_obj_type = 'ALT'

	if vDataType = "feature"
	then
	  let vGenotypeFtrCount = (Select count(*) 
			  	     from genotype_feature
			  	     where genofeat_feature_zdb_id = vOldZDBid);


	  if vGenotypeFtrCount > 0
	  	then 

	  	let vNewFtrAbbrev = (select feature_abbrev
				from feature
				where feature_zdb_id = vNewZdbId);

	 	 let vOldFtrAbbrev = (select feature_abbrev
				 from feature
				 where feature_zdb_id = vOldZdbId) ;

		update genotype 
		  set (geno_display_name, geno_handle) = 
			(replace(geno_display_name,vOldFtrAbbrev,
					vNewFtrAbbrev),
			replace(geno_handle,vOldFtrAbbrev,vNewFtrAbbrev))
		  where exists (Select 'x'
				  from genotype_feature
				  where genofeat_geno_zdb_id = geno_zdb_id
				  and genofeat_feature_zdb_id = vNewZDBid);

	  end if ; -- end if vGenotypeFtrCount > 0

 	elif vDataType = 'marker'
	then

	  let vNewMrkrAbbrev = (select mrkr_abbrev
				from marker
				where mrkr_zdb_id = vNewZdbId);

	  let vOldMrkrAbbrev = (select mrkr_abbrev
				 from marker
				 where mrkr_zdb_id = vOldZdbId) ;

	  let vFmrelCount = (select count(*)
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

end procedure ;