create trigger marker_go_term_update_trigger
  update of mrkrgo_go_term_zdb_id 
  on marker_go_term
  referencing new as new_go_term
  for each row (
      execute procedure p_goterm_not_obsolete (
	new_go_term.mrkrgo_go_term_zdb_id
	)
);