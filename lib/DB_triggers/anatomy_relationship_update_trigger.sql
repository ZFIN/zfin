-- CREATE ANATOMY_RELATIONSHIP UPDATE TRIGGER
-------------------------------------------
-- The trigger ensures an update on a relationship 
-- pair is still following the parent child stage
-- range rule. 

create trigger anatomy_relationship_update_trigger
	update on anatomy_relationship
	referencing new as new_anatomy_relationship
	for each row (
		execute procedure p_check_anatrel_stg_consistent (
					new_anatomy_relationship.anatrel_anatitem_1_zdb_id,
					new_anatomy_relationship.anatrel_anatitem_2_zdb_id,
					new_anatomy_relationship.anatrel_dagedit_id)
				);
