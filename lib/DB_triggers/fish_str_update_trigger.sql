create trigger fishstr_str_update_trigger update of fishstr_str_zdb_id, fishstr_fish_zdb_id
    on fish_str
    after
        (
        execute procedure p_update_related_fish_name() 
    );