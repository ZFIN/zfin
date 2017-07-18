--P_check_zdbid_in_genedom.sql
--------------------------------
  --procedure that checks to see that the object type of a zdb_id 
  --exists in the marker_type_group 'GENEDOM' 

  create procedure p_check_zdb_id_in_genedom (vZdbId varchar(50))
  
      define vType	varchar(10) ;
      define vOk	integer ;

      let vType = get_obj_type(vZdbId) ;
  
      let vOk = (select count(*)
	               from marker_type_group_member
		       where mtgrpmem_mrkr_type_group = 'GENEDOM'
		       and mtgrpmem_mrkr_type = vType ) ;
      if vOk == 0

      then 
        raise exception -746, 0, 'FAIL!: zdb_id is not in GENEDOM' ;
    
      end if ;

  end procedure ;
