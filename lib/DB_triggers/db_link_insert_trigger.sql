create trigger db_link_insert_trigger
  insert on db_link
  referencing new as new_db_link
  for each row (

  execute function 
      scrub_char(new_db_link.dblink_acc_num) into dblink_acc_num,

  execute procedure 
      p_dblink_has_parent(new_db_link.dblink_linked_recid),
 
   execute procedure 
      p_check_caps_acc_num(new_db_link.dblink_fdbcont_zdb_id,
			   new_db_link.dblink_acc_num),

    execute function
	get_genbank_dblink_length_type (new_db_link.dblink_acc_num,
					new_db_link.dblink_length,
					new_db_link.dblink_fdbcont_zdb_id)
      into db_link.dblink_fdbcont_zdb_id, db_link.dblink_length,

  execute procedure 
      p_one_ortho_per_db(new_db_link.dblink_fdbcont_zdb_id,
			 new_db_link.dblink_linked_recid),

  execute function get_dblink_acc_num_display(
			new_db_link.dblink_fdbcont_zdb_id,
			new_db_link.dblink_acc_num) 
      into db_link.dblink_acc_num_display

  );