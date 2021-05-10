--liquibase formatted sql
--changeset pkalita:updateFishNames

-- many fish created at the dawn of the fish era (e.g. with ids like 'ZDB-FISH-150901-%'
-- have spaces missing in their fish_name. No fish created since then seem to have that
-- problem, so do a one-time sweep on all the badly named fish to fix case 14118.

CREATE TEMP TABLE tmp_fish_names (
  tmp_fish_id VARCHAR(50),
  tmp_fish_name VARCHAR(255)
);

INSERT INTO tmp_fish_names
SELECT fish_zdb_id, get_fish_name(fish_zdb_id)
FROM fish
WHERE fish_name != get_fish_name(fish_zdb_id);

UPDATE fish
SET fish_name = (
  SELECT tmp_fish_name
  FROM tmp_fish_names
  WHERE tmp_fish_id = fish_zdb_id
)
WHERE EXISTS (
  SELECT 'x'
  FROM tmp_fish_names
  WHERE tmp_fish_id = fish_zdb_id
);

DROP TABLE tmp_fish_names;