create trigger marker_go_term_insert_trigger 
  insert on marker_go_term
  referencing new as new_mrkr_goterm
    for each row (
      execute procedure p_goterm_not_obsolete (
	new_mrkr_goterm.mrkrgo_go_term_zdb_id
	)
);