-- CREATE TERM_RELATIONSHIP INSERT TRIGGER
-----------------------------------------------
-- The trigger ensures the new relationship pair
-- are honoring the parent child stage range rule.

create trigger term_relationship_insert_trigger
	insert on term_relationship
	referencing new as new_term_relationship
	for each row (
		execute procedure p_check_anatrel_stg_consistent (
					new_term_relationship.termrel_term_1_zdb_id,
					new_term_relationship.termrel_term_2_zdb_id,
					new_term_relationship.termrel_type)
				);

