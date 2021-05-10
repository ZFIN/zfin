  create or replace function p_mrel_grpmem_correct
    (vMarker1 VARCHAR(30),
     vMarker2 VARCHAR(30), 
     vMarkerRelType varchar(255)) 
  returns void as $$

     declare ok integer;
      vObjectType1 varchar(30);
      vObjectType2 varchar(30);
      vValidMarkerGroup1 varchar(30);
      vValidMarkerGroup2 varchar(30);

begin 
     select get_obj_type(vMarker1) into vObjectType1;
     select get_obj_type(vMarker2) into vObjectType2;
  
     select mreltype_mrkr_type_group_1 
       into vValidMarkerGroup1 
       from marker_relationship_type 
       where mreltype_name = vMarkerRelType;

     select mreltype_mrkr_type_group_2 
       into vValidMarkerGroup2 
       from marker_relationship_type 
       where mreltype_name = vMarkerRelType; 

     -- now need to check that each objecttype is valid in 
     -- markergroup via marker_type_group_member

     if exists (select * 
                  from marker_type_group_member 
                  where mtgrpmem_mrkr_type = vObjectType1
		  and mtgrpmem_mrkr_type_group = vValidMarkerGroup1)
     then
       if exists (select * 
                    from marker_type_group_member 
                    where mtgrpmem_mrkr_type = vObjectType2
	     	    and mtgrpmem_mrkr_type_group = vValidMarkerGroup2)

       then ok := 1;

         else 
	   raise exception 'FAIL!: marker2 not in correct marker group';
	
	 end if;

       else 
         raise exception 'FAIL!: marker1 not in correct marker group';
  
       end if;

end
$$ LANGUAGE plpgsql
