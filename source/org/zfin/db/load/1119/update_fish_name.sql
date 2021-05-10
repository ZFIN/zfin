--liquibase formatted sql
--changeset sierra:update_fish_name.sql

update fish
  set fish_name = get_fish_name(fish_zdb_id)
 where fish_zdb_id in ('ZDB-FISH-200325-68',
'ZDB-FISH-200325-51',
'ZDB-FISH-200325-47',
'ZDB-FISH-200325-54');


