create trigger pub_tracking_history_insert_trigger 
 insert on pub_tracking_history 
referencing new as new_pth
for each row (execute function updateMaxStatusAsCurrent(new_pth.pth_pub_zdb_id) into pth_status_is_current,
    execute procedure checkPubTrackingLocationOwner (new_pth.pth_pub_zdb_id,
						new_pth.pth_status_id,
						new_pth.pth_location_id,
						new_pth.pth_claimed_by),
    execute procedure updatePubCompletionDate(new_pth.pth_pub_zdb_id,
						new_pth.pth_status_id),
    execute procedure updatePubIndexedDate(new_pth.pth_pub_zdb_id,
						new_pth.pth_status_id)
);
