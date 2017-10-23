CREATE OR REPLACE FUNCTION month(date DATE)
  RETURNS VARCHAR AS $objType$

BEGIN

  RETURN to_char(date, 'MM');

END
$objType$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION month(date TIMESTAMP)
  RETURNS VARCHAR AS $objType$

BEGIN

  RETURN to_char(date, 'MM');

END
$objType$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION year(date TIMESTAMP)
  RETURNS VARCHAR AS $objType$

BEGIN

  RETURN to_char(date, 'YYYY');

END
$objType$ LANGUAGE plpgsql;

