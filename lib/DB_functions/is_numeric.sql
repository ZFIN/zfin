-- Tries to cast a string to a numeric value and returns true if it succeeds, false otherwise.
CREATE OR REPLACE FUNCTION is_numeric(text) RETURNS bool AS $BODY$
DECLARE castVariable NUMERIC;
BEGIN
    castVariable = $1::NUMERIC;
    RETURN TRUE;
EXCEPTION WHEN others THEN
    RETURN FALSE;
END;
$BODY$
LANGUAGE 'plpgsql' IMMUTABLE STRICT;
