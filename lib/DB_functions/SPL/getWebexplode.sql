create procedure
getWebExplode(name varchar(100), params lvarchar(1000))

  returning clob;

  DEFINE contents clob;

  SELECT WebExplode(object,params)
  INTO contents
  FROM WebPages where ID = name;

  RETURN contents;

end procedure
	