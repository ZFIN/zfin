--P_CHECK_ZDB_OBJECT_TABLE
  ------------------------------------------------------
  --procedure that checks to make sure tables and columns in 
  --zdb_object_type table exist in systables and syscolumns.
  --REPLACES:
  --sub zdbObjectHomeTableColumnExist 

  create procedure p_check_zdb_object_table (vTableName varchar(128), 
	  				     vColumnName varchar(35))
 
  define vOkInSystables		integer;
  define vOkInSyscolumns	integer;
  define vTableId		integer;

  let vTableId = (select tabid 
		   from systables 
		   where tabname = vTableName);

  let vOkInSystables = (select count(*) 
			  from systables 
			  where tabname = vTableName);

  if vOkInSystables < 1 then
    raise exception -746, 0, "FAIL!: table name not in systables";
  else 
	let vOkInSyscolumns = (select count(*) 
	 			 from syscolumns, systables 
  				 where syscolumns.tabid = systables.tabid
			         and systables.tabid = vTableid
				 and syscolumns.colname = vColumnName);

	if vOkInSyscolumns < 1 then
	  raise exception -746, 0, "FAIL!: column name not in syscolumns";
	end if;

  end if;
 
  end procedure;
