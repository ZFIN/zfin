create or replace function updateMaxStatusAsCurrent (workingPub text)
returns boolean as $true$
 

  declare now_time timestamp := current_timestamp;

begin 
  update pub_tracking_history
    set (pth_status_is_current, pth_status_made_non_current_date, pth_days_in_status) =('f', now_time, date(now_time)-date(pth_status_insert_date))
    where pth_status_is_current = 't'
    and pth_pub_zdb_id = workingPub;

return 't';
end

$true$ LANGUAGE plpgsql;
