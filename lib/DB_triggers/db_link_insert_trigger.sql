create trigger db_link_insert_trigger
  insert on db_link
  referencing new as new_db_link
  for each row (
    execute function 
      scrub_char(new_db_link.acc_num) into acc_num,
    execute function 
      get_dblink_acc_num_display(new_db_link.db_name, new_db_link.acc_num)
      into dblink_acc_num_display
  );
