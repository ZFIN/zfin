create trigger fishstr_str_update_trigger update of fishstr_str_zdb_id, fishstr_fish_zdb_id
    on fish_str referencing old as old_fstr
    new as new_fstr
    for each row
        (
        execute procedure p_update_related_fish_names(new_fstr.fishstr_str_zdb_id) 
    ));