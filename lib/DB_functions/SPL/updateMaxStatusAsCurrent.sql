create function updateMaxStatusAsCurrent (workingPub varchar(50))
returning boolean;
  

  update pub_tracking_history
    set pth_status_is_current ='f'
    where pth_status_is_current = 't'
    and pth_pub_zdb_id = workingPub
    and pth_status_is_current = 't';

return 't';
end function;
