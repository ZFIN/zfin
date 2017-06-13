-- Return the next available id for a given zobjtype
CREATE OR REPLACE FUNCTION get_id(name varchar) RETURNS varchar AS $$
DECLARE
  num integer;
  date text;
  prev_date date;
  seq_name regclass;
BEGIN
  seq_name := name || '_seq';
  SELECT zobjtype_day INTO prev_date FROM zdb_object_type WHERE zobjtype_name = name;
  IF prev_date <> current_date THEN
    UPDATE zdb_object_type SET zobjtype_day = current_date WHERE zobjtype_name = name;
    PERFORM setval(seq_name, 1, false);
  END IF;
  date := to_char(current_timestamp, 'YYMMDD');
  num := nextval(seq_name);
  RETURN 'ZDB-' || name || '-' || date || '-' || CAST(num AS text);
END;
$$ LANGUAGE plpgsql;
