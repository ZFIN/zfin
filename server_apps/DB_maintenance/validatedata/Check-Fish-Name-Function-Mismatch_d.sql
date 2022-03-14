SELECT
    fish_zdb_id,
    fish_name,
    get_fish_name (fish_zdb_id) AS computed_fish_name
FROM
    fish
WHERE (fish_name <> get_fish_name (fish_zdb_id)
    OR trim(fish_name) = ''
    OR trim(get_fish_name (fish_zdb_id)) = '')
  AND fish_name <> 'Cooch Behar' -- exception
ORDER BY
    fish_zdb_id