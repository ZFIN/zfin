-- CREATE ANATOMY_ITEM UPDATE TRIGGER
----------------------------------------------------------
-- The stage window check replaced the anatomyItemStageWindowConsistent 
-- test in validatedata.pl

create trigger anatomy_items_name_update_trigger 
  update of anatitem_name on anatomy_item
    referencing new as new_anatomy_item
    for each row (
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
