  --P_CHECK_DB_LINKS.SQL
  -------------------------------------------------------------
  --procedure that checks to see that there is at least one record in either 
  --the marker,  or term table for each linked_recid in db_link.  
  --This procedure checks thetable first, then the marker table.
  --we can not do a foreign key to this table b/c marker can be 
  --parent table.
  --REPLACES:
  --sub 

  create or replace function p_dblink_has_parent (vLinkedRecid varchar(55))
  returns void as $$
  
  declare vHasParent	 	boolean :='f';
  begin 
  -- check marker table
  select 't'
    into vHasParent
    from marker
   where mrkr_zdb_id = vLinkedRecid;

    if (not vHasParent) then 
	
	-- check term table
	select 't'
          into vHasParent
          from term
         where term_zdb_id = vLinkedRecid;
  
	if (not vHasParent) then 
   	    raise exception 'FAIL!: dblink must have marker or term parent';
  	end if;
    end if ;	

   end
$$ LANGUAGE plpgsql
