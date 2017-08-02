-- Return the next available id for a given zobjtype
CREATE OR REPLACE FUNCTION get_id(obj_name varchar) RETURNS text AS $$
DECLARE
  num integer;
  date text;
  prev_date date;
  seq_name text;
BEGIN
  seq_name = (select lower(obj_name)) || '_seq';
  raise notice 'Value: %', seq_name;
  SELECT zobjtype_day INTO prev_date FROM zdb_object_type WHERE zobjtype_name = obj_name;
  IF prev_date <> current_date THEN
    UPDATE zdb_object_type SET zobjtype_day = current_date WHERE zobjtype_name = obj_name;
    PERFORM setval(seq_name, 1, false);
    raise notice 'next date found';
  END IF;
  date = (select to_char(current_timestamp, 'YYMMDD'));
  num = (select nextval(seq_name));
  raise notice 'Value: %', num;
  RETURN 'ZDB-' || obj_name || '-' || date || '-' || CAST(num AS text);
END;
$$ LANGUAGE plpgsql;
