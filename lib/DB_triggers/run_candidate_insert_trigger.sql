create trigger run_candidate_insert_trigger insert on
    run_candidate referencing new as new_runcan
    for each row
    (
        execute function increment_candidate_occurrences (
		new_runcan.runcan_cnd_zdb_id) into runcan_occurrence_order
    );