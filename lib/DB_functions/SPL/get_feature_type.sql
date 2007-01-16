create function get_feature_type(featureZdbId varchar(50)) 
	returning varchar(30);

  define vObjType like feature.feature_type ;
  
  let vObjType = (select feature_type
			from feature
			where feature_zdb_id = featureZdbId);

  return vObjType ;

end function ;