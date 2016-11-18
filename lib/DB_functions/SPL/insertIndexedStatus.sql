create procedure insertIndexedStatus (vPubZdbId varchar(50),
       		 			 vPubStatusId int8,
					 vStatusSetBy varchar(50)
					 )

define status varchar(50);
let status = (Select pts_status from pub_tracking_Status
    	     	     where pts_pk_id = vPubStatusId);

if ((status = 'READY_FOR_CURATION' or status like 'CLOSED%') and exists (Select 'x' from pub_tracking_history, pub_tracking_status where pth_status_id = pts_pk_id and pts_status = 'INDEXING') )
  then 
  insert into pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_set_by) 
  select vPubZdbId, (Select pts_pk_id from pub_tracking_status
  	 	    	    where pts_status = 'INDEXED'), vStatusSetBy
   from single;

end if;
end procedure;
