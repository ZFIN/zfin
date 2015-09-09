create trigger fish_str_insert_trigger insert on
   fish_str referencing new as new_fishstr
    for each row
        (
        execute procedure p_update_related_fish_for_str(new_fishstr.fishstr_str_zdb_id) 
    );