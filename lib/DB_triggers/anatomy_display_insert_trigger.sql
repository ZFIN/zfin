--anatomy_display_insert_trigger
----------------------------------------------------------
create trigger anatomy_display_insert_trigger 
  insert on anatomy_display
  referencing new as new_anatdisp
  for each row (
        execute function scrub_char(new_anatdisp.anatdisp_item_name) 
	  into anatdisp_item_name
      );