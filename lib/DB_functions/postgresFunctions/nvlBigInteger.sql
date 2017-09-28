create or replace function nvl(name bigInt, defaultValue bigInt) returns bigInt as $objType$

begin

  if name IS NULL THEN
    return defaultValue;
  end if;

  return name;

 end
$objType$ LANGUAGE plpgsql;

