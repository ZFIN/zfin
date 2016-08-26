create trigger pub_tracking_history_insert_trigger 
 insert on pub_tracking_history 
referencing new as new_pth
for each row (execute function updateMaxStatusAsCurrent(new_pth.pth_pub_zdb_id) into pth_status_is_current);
