-- CREATE ANATOMY_ITEM UPDATE TRIGGER
----------------------------------------------------------
-- The stage window check replaced the anatomyItemStageWindowConsistent 
-- test in validatedata.pl

create trigger anatomy_items_update_trigger 
  update on anatomy_item
    referencing new as new_anatomy_item
    for each row (
		execute procedure p_stg_hours_consistent
  	  		(new_anatomy_item.anatitem_start_stg_zdb_id, 
           		 new_anatomy_item.anatitem_end_stg_zdb_id),
		execute procedure p_check_anat_anatrel_stg_consistent
			(new_anatomy_item.anatitem_zdb_id, 
			 new_anatomy_item.anatitem_start_stg_zdb_id,
			 new_anatomy_item.anatitem_end_stg_zdb_id),
                -- scrub name
        	execute function 
	  		scrub_char(new_anatomy_item.anatitem_name)
			into anatitem_name,
                -- then push it to name order and lower columns
        	execute function 
	  		zero_pad(new_anatomy_item.anatitem_name)
			into anatitem_name_order,
		execute function 
	  		lower(new_anatomy_item.anatitem_name)
			into anatitem_name_lower
    );
