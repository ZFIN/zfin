create trigger dalias_alias_update_trigger
  update of dalias_alias on data_alias
  referencing new as new_data_alias
  for each row (
    execute function
      scrub_char(new_data_alias.dalias_alias) into dalias_alias,
    execute function
      lower(new_data_alias.dalias_alias) into dalias_alias_lower
  );
