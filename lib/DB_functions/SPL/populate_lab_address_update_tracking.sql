
create procedure populate_lab_address_update_tracking (labZdbId varchar(50), oldAddress lvarchar(450), newAddress lvarchar(450))

    if (oldAddress != newAddress) then

        insert into lab_address_update_Tracking (laut_lab_zdb_id, laut_previous_address, laut_new_address)
                values (labZdbId, oldAddress, newAddress);
    end if;

end procedure;
