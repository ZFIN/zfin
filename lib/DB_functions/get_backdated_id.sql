-- Return the next available id for a given zobjtype
CREATE OR REPLACE FUNCTION get_backdated_id(obj_name varchar, back_date varchar) RETURNS text AS $$
DECLARE
  calculated_id text;
  calculated_id_prefix text;
  calculated_id_seq int;
BEGIN

    calculated_id_prefix := 'ZDB-' || obj_name || '-' || back_date;

    -- get the next available number
    SELECT COALESCE(MAX(get_sequence_from_id(mrkr_zdb_id)::integer), 0)
    INTO calculated_id_seq
    FROM marker
    WHERE mrkr_zdb_id LIKE calculated_id_prefix || '-%';

    calculated_id_seq := calculated_id_seq + 1;

    calculated_id := calculated_id_prefix || '-' || calculated_id_seq;

    RETURN calculated_id;

END
$$ LANGUAGE plpgsql;
