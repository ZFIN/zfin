--P_check_zdbid_in_genedom.sql
--------------------------------
  --procedure that checks to see that the object type of a zdb_id 
  --exists in the marker_type_group 'GENEDOM' 

  create or replace function p_check_zdb_id_in_genedom (vZdbId text)
  returns void as $$

      declare vType	varchar(10) := get_obj_type(vZdbId);
      	      vOk	integer := (select count(*)
	               		   	   from marker_type_group_member
		       			   where mtgrpmem_mrkr_type_group = 'GENEDOM'
		       			   and mtgrpmem_mrkr_type = vType );

  begin 
      if vOk = 0

      then 
        raise exception 'FAIL!: oevdisp_gene_zdb_id must have type valid in marker_group: GENEDOM' ;
    
      end if ;
  end

$$ LANGUAGE plpgsql
