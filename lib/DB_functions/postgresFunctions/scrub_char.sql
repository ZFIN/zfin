-- Scrub input clean of whitespace and control characters
CREATE OR REPLACE FUNCTION scrub_char(to_clean varchar) RETURNS varchar AS $$
BEGIN
  RETURN regexp_replace(regexp_replace(trim(to_clean), '\s+', ' ', 'g' ), E'[\\x00-\\x1F]+', '', 'g' );
END;
$$ LANGUAGE plpgsql;
