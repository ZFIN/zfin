-- Return a string with all numbers zero padded to improve sorting
CREATE OR REPLACE FUNCTION zero_pad(to_pad varchar) RETURNS varchar AS $$
DECLARE
  pad_len int := 10;
BEGIN
  RETURN regexp_replace(regexp_replace(to_pad, '[0-9]+', repeat('0', pad_len) || '\&', 'g'), 
                           '[0-9]*([0-9]{' || pad_len || '})', '\1', 'g');
END;
$$ LANGUAGE plpgsql;
