-- CREATE ANATOMY_ITEM INSERT TRIGGER
----------------------------------------------------------
-- The stage window check replaced the anatomyItemStageWindowConsistent 
-- test in validatedata.pl
create trigger anatomy_items_insert_trigger 
  insert on anatomy_item
  referencing new as new_anatomy_item
  for each row (
      execute procedure p_stg_hours_consistent
  	(new_anatomy_item.anatitem_start_stg_zdb_id,
	 new_anatomy_item.anatitem_end_stg_zdb_id),
      execute function 
	zero_pad(new_anatomy_item.anatitem_name) 
	into anatitem_name_order,
      execute function
	scrub_char(new_anatomy_item.anatitem_name)
	into anatitem_name,
      execute function 
	lower(new_anatomy_item.anatitem_name) 
	into anatitem_name_lower,
      execute function
	scrub_char(new_anatomy_item.anatitem_name_lower)
	into anatitem_name_lower,
      execute function
	scrub_char(new_anatomy_item.anatitem_name_order)
	into anatitem_name_order
      );
