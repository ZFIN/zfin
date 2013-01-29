-- CREATE ANATOMY_RELATIONSHIP UPDATE TRIGGER
-------------------------------------------
-- The trigger ensures an update on a relationship 
-- pair is still following the parent child stage
-- range rule. 

create trigger term_relationship_update_trigger
	update on term_relationship
	referencing new as new_term_relationship
	for each row (
		execute procedure p_check_anatrel_stg_consistent (
					new_term_relationship.termrel_term_1_zdb_id,
					new_term_relationship.termrel_term_2_zdb_id,
					new_term_relationship.termrel_type)
				);
