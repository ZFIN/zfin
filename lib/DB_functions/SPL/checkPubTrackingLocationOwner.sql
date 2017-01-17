create procedure checkPubTrackingLocationOwner(vPthPubZdbId varchar(50), vPthStatusId int8, vPthLocationId int8, vPthClaimedBy varchar(50))

       define status varchar(100);

       let status = (Select pts_status from pub_tracking_status
       	   	    	    where pts_pk_id = vPthStatusId);

       if status in ('Curating','Indexing','Indexed','Curated') 
        and (vPthLocationId is null or vPthClaimedBy is null)
          then 
       	    raise exception -746,0,'FAIL!: status of curating or indexing need locations';
       end if;
       
end procedure;
