
create or replace function populate_lab_address_update_tracking (labZdbId text, oldAddress text, newAddress text)
returns void as $$

begin
    if (oldAddress != newAddress) then

        insert into lab_address_update_Tracking (laut_lab_zdb_id, laut_previous_address, laut_new_address)
                values (labZdbId, oldAddress, newAddress);
    end if;
end
$$ LANGUAGE plpgsql
