SELECT
    fish_zdb_id,
    fish_name,
    get_fish_name (fish_zdb_id) AS computed_fish_name
FROM
    fish
WHERE (
            fish_name <> get_fish_name (fish_zdb_id)
        OR trim(fish_name) = ''
        OR trim(get_fish_name (fish_zdb_id)) = ''
        OR trim(fish_name) = '(AB)' -- basically a blank name, but with a background
        OR trim(get_fish_name (fish_zdb_id)) = '(AB)'
        OR left(trim(fish_name),1) = '+' -- fish name starts with +, so a blank name and STRs
        OR left(trim(get_fish_name (fish_zdb_id)),1) = '+'
        OR trim(fish_name) = '_' -- placeholder for unhandled blank name
        OR trim(get_fish_name (fish_zdb_id)) = '_'
    )
  AND fish_name <> 'Cooch Behar' -- exception
ORDER BY
    fish_zdb_id