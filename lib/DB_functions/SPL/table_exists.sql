drop function table_exists;

create function
table_exists(table_name varchar(128))

  -- table name can be a permanent or temporary table.

  returning boolean;	-- 't' if it exists, 'f' if not.

  define tableExists       char(1);

  if exists ( select * 
		from sysmaster:systabnames
		where dbsname = '<!--|DB_NAME|-->' 
		  and tabname = table_name) then
    let tableExists = 't';
  else
    let tableExists = 'f';
  end if

  return tableExists;

end function;

update statistics for function table_exists;
