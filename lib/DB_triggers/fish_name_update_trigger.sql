create trigger fish_name_update_trigger update of fish_name on fish
  referencing new as new_fish
 for each row (
     execute function scrub_char(new_fish.fish_name)
     	     into fish.fish_name,
     execute function zero_pad (new_fish.fish_name)
     	     into fish.fish_name_order --,
--     execute function regen_names_fish(new_fish.fish_Zdb_id),
    --	execute function update_fish_sort_order(new_fish.fish_zdb_id)
    --into fish.fish_complexity_order 
);