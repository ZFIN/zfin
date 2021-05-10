create or replace function get_feature_type(featureZdbId varchar) returns varchar as $vObjType$ 

   declare vObjType  feature.feature_type%TYPE ;

  begin
  vObjType := (select feature_type
			from feature
			where feature_zdb_id = featureZdbId);

  return vObjType ;
 end
$vObjType$ LANGUAGE plpgsql;
