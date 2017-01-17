create trigger salias_alias_update_trigger update 
    of salias_alias on source_alias referencing new 
    as new_source_alias
    for each row
        (
        execute function scrub_char(new_source_alias.salias_alias 
    ) into source_alias.salias_alias,
        execute function lower(new_source_alias.salias_alias 
    ) into source_alias.salias_alias_lower);
