--P_CHECK_ZDB_OBJECT_TABLE
  ------------------------------------------------------
 --procedure that checks to make sure tables and columns in 
  --zdb_object_type table exist in pg_tables and pg_attributes.
  --REPLACES:
  --sub zdbObjectHomeTableColumnExist 

  create or replace function p_check_zdb_object_table (vSchemaName text,
	  				     vTableName text,
	  				     vColumnName text)
  returns void as $$
  declare vOkInSystables		integer;
   vOkInSyscolumns	integer;
   vTableId		integer;
   vSchema		text := COALESCE(NULLIF(vSchemaName, ''), 'public');
  begin
   vTableId = (select c.oid
                  from pg_class c
                  join pg_namespace n on n.oid = c.relnamespace
                  where n.nspname = vSchema and c.relname = vTableName);

   vOkInSystables = (select count(*) 
			  from pg_tables
			  where schemaname = vSchema and tablename = vTableName);
  raise notice 'vOkInSystables: %', vOkInSystables;
  raise notice 'vTableid: %', vTableid;

  if vOkInSystables < 1 then
    raise exception 'FAIL!: table name not in systables';
  else 
	 vOkInSyscolumns = (select count(*) 
	 			 from pg_attribute 
  				 where attrelid = vTableid
				 and attname = vColumnName);
	raise notice 'vColumnName: %', vColumnName;

	if vOkInSyscolumns < 1 then
	  raise exception 'FAIL!: column name not in syscolumns';
	end if;

  end if;
 end
$$ LANGUAGE plpgsql
