create or replace function updatePubCompletionDate (vPubZdbId text,
       		 			 vPubStatusId int8)
returns void as $$

--procedure assumes that on insert of pub_tracking_history, only the
--closed status indicates a pub is finished.  any other status means
--it is not finished and thus should not have a pub_completion_date.
--this may mean that it was once "completed" but is no longer completed based
--on curator actions in the pub tracker -- aka: pub_history_tracking table.

declare status varchar(50) := (Select pts_status from pub_tracking_Status
    	     	     where pts_pk_id = vPubStatusId);
begin 
if (status like 'CLOSED%')
  then 
  update publication 
  	 set pub_completion_date = now()
	 where zdb_id = vPubZdbId;
 else 
 update publication
	set pub_completion_date = null
	where zdb_id = vPubZdbId;

end if;

end;

$$ LANGUAGE plpgsql;
