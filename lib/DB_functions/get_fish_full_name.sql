CREATE OR REPLACE FUNCTION get_fish_full_name(vFishZdbId     text,
                                              vFishGenoZdbId text,
                                              vFishName      varchar)
  RETURNS varchar AS $fullName$

DECLARE backgroundList varchar := get_genotype_backgrounds(vFishGenoZdbId);
        fullName       varchar;

BEGIN
  IF (backgroundList IS NOT NULL AND backgroundList != '')
  THEN
    fullName := vFishName || '(' || backgroundList || ')';
  ELSE
    fullName := vFishName;
  END IF;

  RETURN fullName;
END
$fullName$ LANGUAGE plpgsql;

