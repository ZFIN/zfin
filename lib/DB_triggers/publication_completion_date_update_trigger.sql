create trigger publication_completion_date_update_trigger
  update of pub_completion_date on publication
  referencing old as old_publication new as new_publication
  for each row (
            execute procedure p_delete_curator_session(new_publication.zdb_id,
      new_publication.pub_completion_date)
);
