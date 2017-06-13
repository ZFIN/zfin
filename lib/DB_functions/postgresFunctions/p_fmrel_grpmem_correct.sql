  create or replace function p_fmrel_grpmem_correct
    (vFeature VARCHAR(30),
     vMarker VARCHAR(30), 
     vFeatureMarkerRelType varchar(255)) 
  returns void as $$

     declare ok integer;
      vObjectType1 varchar(30) := get_feature_type(vFeature);
      vObjectType2 varchar(30):= get_obj_type(vMarker);
      vValidFeatureGroup varchar(30);
      vValidMarkerGroup varchar(30);

     begin 
  
     select fmreltype_ftr_type_group
       into vValidFeatureGroup 
       from feature_marker_relationship_type 
       where fmreltype_name = vFeatureMarkerRelType;

     select fmreltype_mrkr_type_group 
       into vValidMarkerGroup 
       from feature_marker_relationship_type 
       where fmreltype_name = vFeatureMarkerRelType; 

     -- now need to check that each objecttype is valid in 
     -- featuregroup via feature_type_group_member

     if exists (select * 
                  from feature_type_group_member 
                  where ftrgrpmem_ftr_type = vObjectType1
		  and ftrgrpmem_ftr_type_group = vValidFeatureGroup
		  )

    then
       if exists (select * 
                    from marker_type_group_member 
                    where mtgrpmem_mrkr_type = vObjectType2
	     	    and mtgrpmem_mrkr_type_group = vValidMarkerGroup)

       then ok = 1;

       else 
	   raise exception 'FAIL!: marker not in correct marker group';
	
       end if;

     else 

        raise exception 'FAIL!: vObjectType1 feature not in correct vValidFeatureGroup feature group';
  
     end if ;  
 
end
$$ LANGUAGE plpgsql
