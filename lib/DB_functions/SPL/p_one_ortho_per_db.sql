create procedure p_one_ortho_per_db (vFdbcontZdbId varchar(50),
				     vLinkedRecid  varchar(50)) 

define vOk integer ;    
define vOrtho varchar(50) ;

  let vOrtho = get_obj_type(vLinkedRecid) ;
  
  if (vOrtho = 'ORTHO') then

	let vOk =  (select count(*)
 			from db_link, foreign_db_contains	
			where dblink_fdbcont_zdb_id = fdbcont_zdb_id
			and dblink_linked_recid = vLinkedRecid
			and dblink_fdbcont_zdb_id = vfdbcontZdbId) ;
    if (vOk == 1)	  

      then
     
       let vOk = 0;
       
    else 
  
       raise exception -746,0, 'FAIL!: We already have an ortho from this db!';

    end if

  end if

end procedure ;
