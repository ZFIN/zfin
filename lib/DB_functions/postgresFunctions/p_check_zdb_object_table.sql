--P_CHECK_ZDB_OBJECT_TABLE
  ------------------------------------------------------
  --procedure that checks to make sure tables and columns in 
  --zdb_object_type table exist in systables and syscolumns.
  --REPLACES:
  --sub zdbObjectHomeTableColumnExist 

  create or replace function p_check_zdb_object_table (vTableName varchar(128), 
	  				     vColumnName varchar(35))
  returns void as $$
  declare vOkInSystables		integer;
   vOkInSyscolumns	integer;
   vTableId		integer;
  begin
   vTableId = (select tabid 
		   from systables 
		   where tabname = vTableName);

   vOkInSystables = (select count(*) 
			  from systables 
			  where tabname = vTableName);

  if vOkInSystables < 1 then
    raise exception 'FAIL!: table name not in systables';
  else 
	 vOkInSyscolumns = (select count(*) 
	 			 from syscolumns, systables 
  				 where syscolumns.tabid = systables.tabid
			         and systables.tabid = vTableid
				 and syscolumns.colname = vColumnName);

	if vOkInSyscolumns < 1 then
	  raise exception 'FAIL!: column name not in syscolumns';
	end if;

  end if;
 end
$$ LANGUAGE plpgsql
