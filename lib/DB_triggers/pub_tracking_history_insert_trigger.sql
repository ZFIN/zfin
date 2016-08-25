create trigger pub_tracking_history_insert_trigger 
 insert on pub_tracking_history 
 before (execute procedure updateCurrentPubStatus())
 after (execute procedure updateMaxStatusAsCurrent())
;
