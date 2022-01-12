select fish_zdb_id, fish_name, get_fish_name(fish_zdb_id) as computed_fish_name from fish
where  fish_name <> get_fish_name(fish_zdb_id)
and fish_name <> 'Cooch Behar' -- exception
order by fish_zdb_id

