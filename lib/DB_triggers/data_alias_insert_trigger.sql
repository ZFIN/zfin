create trigger data_alias_insert_trigger
  insert on data_alias
  referencing new as new_data_alias
  for each row (
    execute function 
      scrub_char(new_data_alias.dalias_alias) into dalias_alias
  );
