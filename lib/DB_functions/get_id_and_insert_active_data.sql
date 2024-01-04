-- Return the next available id for a given zobjtype and insert a new active data record
CREATE OR REPLACE FUNCTION get_id_and_insert_active_data(obj_name varchar) RETURNS text AS $$
DECLARE
    next_id varchar;
BEGIN
    next_id := get_id(obj_name);
    INSERT INTO zdb_active_data (zactvd_zdb_id) VALUES (next_id);
    RETURN next_id;
END;
$$ LANGUAGE plpgsql;
