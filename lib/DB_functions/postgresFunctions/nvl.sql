create or replace function nvl(name varchar, defaultValue varchar) returns varchar as $objType$

begin

  if name IS NULL THEN
    return defaultValue;
  end if;

  return name;

 end
$objType$ LANGUAGE plpgsql;

