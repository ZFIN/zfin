-- Scrub input clean of whitespace and control characters
CREATE OR REPLACE FUNCTION scrub_char(to_clean varchar)
  RETURNS varchar AS $$
BEGIN
  RETURN
  regexp_replace(
      regexp_replace(
          regexp_replace(
              to_clean,
              '^\s+|\s+$', '', 'g'), -- remove leading and trailing whitespace completely
          '\s+', ' ', 'g'),          -- replace other whitespace with a single space
      E'[\\x00-\\x1F]+', '', 'g'     -- remove invisible control characters
  );
END;
$$ LANGUAGE plpgsql;
