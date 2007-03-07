  --P_CHECK_DB_LINKS.SQL
  -------------------------------------------------------------
  --procedure that checks to see that there is at least one record in either 
  --the marker, orthologue or anatomy_item table for each linked_recid in db_link.  
  --This procedure checks the orthologue table first, then the marker table.
  --we can not do a foreign key to this table b/c orthologue or marker can be 
  --parent table.
  --REPLACES:
  --sub dblinkRecidIsOrthoOrMarker

  create procedure p_dblink_has_parent (vLinkedRecid varchar(55))

  define vHasParent	 	boolean;
  let vHasParent = 'f';

  -- check marker table
  select 't'
    into vHasParent
    from marker
   where mrkr_zdb_id = vLinkedRecid;

  if ( not vHasParent) then 

    -- check orthologue table
    select 't'
      into vHasParent
      from orthologue
     where zdb_id = vLinkedRecid;

    if (not vHasParent) then 
	
	-- check anatomy_item table
	select 't'
          into vHasParent
          from anatomy_item
         where anatitem_zdb_id = vLinkedRecid;
  
	if (not vHasParent) then 
   	    raise exception -746,0,"FAIL!: dblink must have orth or marker parent";
  	end if;
    end if 	

  end if;

  
  end procedure;
