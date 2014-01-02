create trigger lab_address_update_trigger update of 
    address on lab referencing old as old_lab new as 
    new_lab
    for each row
        (
        execute procedure populate_lab_address_update_tracking(new_lab.zdb_id 
    ,old_lab.address ,new_lab.address ));

