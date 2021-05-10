-- Scrub input clean of whitespace and control characters
CREATE OR REPLACE FUNCTION scrub_char(to_clean  integer) RETURNS integer AS $$
BEGIN
  
  RETURN to_clean;
end;
$$ LANGUAGE plpgsql;
