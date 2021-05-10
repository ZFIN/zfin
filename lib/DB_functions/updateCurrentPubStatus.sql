create or replace function updateCurrentPubStatus ()
returns void as $$

begin 

update pub_tracking_history
 set pth_status_is_current = 'f' ;

end

$$ LANGUAGE plpgsql;
