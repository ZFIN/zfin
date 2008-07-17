create trigger clone_problem_type_update_trigger
  update of clone_problem_type on clone 
  referencing new as n
  for each row (execute procedure p_update_clone_relationship(n.clone_mrkr_zdb_id, n.clone_problem_type));