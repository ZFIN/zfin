 create trigger updates_insert_trigger 
   insert on updates 
   referencing new as new_updates
     for each row (
       execute function scrub_char(new_updates.rec_id)
         into rec_id);