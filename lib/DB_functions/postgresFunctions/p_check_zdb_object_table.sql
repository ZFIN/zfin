--P_CHECK_ZDB_OBJECT_TABLE
  ------------------------------------------------------
  --procedure that checks to make sure tables and columns in 
  --zdb_object_type table exist in systables and syscolumns.
  --REPLACES:
  --sub zdbObjectHomeTableColumnExist 

  create or replace function p_check_zdb_object_table (vTableName text, 
	  				     vColumnName text)
  returns void as $$
  declare vOkInSystables		integer;
   vOkInSyscolumns	integer;
   vTableId		integer;
  begin
   vTableId = (select pg_class.oid 
		   from pg_class 
		   where relname = vTableName);

   vOkInSystables = (select count(*) 
			  from pg_tables
			  where tablename = vTableName);

  if vOkInSystables < 1 then
    raise exception 'FAIL!: table name not in systables';
  else 
	 vOkInSyscolumns = (select count(*) 
	 			 from pg_attribute 
  				 where attrelid = vTableid
				 and attname = vColumnName);

	if vOkInSyscolumns < 1 then
	  raise exception 'FAIL!: column name not in syscolumns';
	end if;

  end if;
 end
$$ LANGUAGE plpgsql
