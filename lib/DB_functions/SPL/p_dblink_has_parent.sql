  --P_CHECK_DB_LINKS.SQL
  -------------------------------------------------------------
  --procedure that checks to see that there is at least one record in either 
  --the marker or orthologue table for each linked_recid in db_link.  
  --This procedure checks the orthologue table first, then the marker table.
  --we can not do a foreign key to this table b/c orthologue or marker can be 
  --parent table.
  --REPLACES:
  --sub dblinkRecidIsOrthoOrMarker

  drop procedure p_dblink_has_parent;

  create procedure p_dblink_has_parent (vLinkedRecid varchar(35))

  define vMyCount1	 	integer;
  define vMyCount2		integer;

  let vMyCount1 = (select count(*) 
	  	   from orthologue
		   where vLinkedRecid = orthologue.zdb_id);

  if vMyCount1 == 0 then 
    let vMyCount2 = (select count(*) 
		     from marker
		     where vLinkedRecid = marker.mrkr_zdb_id);
  end if;

  if vMyCount2 == 0 then 
    raise exception -746,0,"FAIL!: dblink must have orth or marker parent";
  end if;

  end procedure;
