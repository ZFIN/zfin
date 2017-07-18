create or replace function checkPubTrackingLocationOwner(vPthPubZdbId text, vPthStatusId int8, vPthLocationId int8, vPthClaimedBy text)
returns void as $$
       declare status varchar(100) := (Select pts_status from pub_tracking_status
       	   	    	    where pts_pk_id = vPthStatusId);
begin
       if status in ('Curating','Indexing','Indexed','Curated') 
        and (vPthLocationId is null or vPthClaimedBy is null)
          then 
       	    raise exception 'FAIL!: status of curating or indexing need locations';
       end if;
end
$$ LANGUAGE plpgsql
