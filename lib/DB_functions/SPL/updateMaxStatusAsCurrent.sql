create function updateMaxStatusAsCurrent (workingPub varchar(50))
returning boolean;
  define now datetime year to second;

  let now = current year to second;

  update pub_tracking_history
    set (pth_status_is_current, pth_status_made_non_current_date, pth_days_in_status) =('f', now, date(now)-date(pth_status_insert_date))
    where pth_status_is_current = 't'
    and pth_pub_zdb_id = workingPub
    and pth_status_is_current = 't';

return 't';
end function;
