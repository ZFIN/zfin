drop trigger publication_update_trigger;

create trigger publication_update_trigger 
  update on publication 
    referencing new as new_publication
    for each row (
      execute function get_pub_mini_ref(new_publication.zdb_id) 
	into publication.pub_mini_ref
    );
