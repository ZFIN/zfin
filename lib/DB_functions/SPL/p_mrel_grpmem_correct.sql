  create procedure p_mrel_grpmem_correct
    (vMarker1 VARCHAR(30),
     vMarker2 VARCHAR(30), 
     vMarkerRelType varchar(255)) 

     define ok integer;
     define vObjectType1 varchar(30);
     define vObjectType2 varchar(30);
     define vValidMarkerGroup1 varchar(30);
     define vValidMarkerGroup2 varchar(30);

     let vObjectType1 = get_obj_type(vMarker1);
     let vObjectType2 = get_obj_type(vMarker2);
  
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

       then let ok = 1;

         else 
	   raise exception -746,0,'FAIL!: marker2 not in correct marker group';
	
	 end if;

       else 
         raise exception -746,0,'FAIL!: marker1 not in correct marker group';
  
       end if;

  end procedure;
