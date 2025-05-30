-- Return the next available id for a given zobjtype and insert a new active data record
CREATE OR REPLACE FUNCTION get_id_and_insert_active_data(obj_name varchar) RETURNS text AS $$
DECLARE
    need_another_id boolean := true;
    next_id varchar := '';
BEGIN
    WHILE need_another_id LOOP
        need_another_id := false;
        next_id := get_id(obj_name);

        BEGIN
            INSERT INTO zdb_active_data (zactvd_zdb_id) VALUES (next_id);
        EXCEPTION
            WHEN unique_violation THEN
                need_another_id := true;
        END;
    END LOOP;

    RETURN next_id;
END;
$$ LANGUAGE plpgsql;