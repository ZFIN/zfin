create or replace function table_exists(tableName varchar(128))
returns boolean as $true$

  declare tableExists       char(1);
  begin 
  if exists (SELECT 'x'  FROM   information_schema.tables 
   		   	 WHERE  table_name = tableName)
 then
   tableExists := 't';
  else
   tableExists := 'f';
  end if;

  return tableExists;
end

$true$ LANGUAGE plpgsql;
