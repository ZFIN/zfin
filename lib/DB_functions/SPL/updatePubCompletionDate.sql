create procedure updatePubCompletionDate (vPubZdbId varchar(50),
       		 			 vPubStatusId int8)

--procedure assumes that on insert of pub_tracking_history, only the
--closed status indicates a pub is finished.  any other status means
--it is not finished and thus should not have a pub_completion_date.
--this may mean that it was once "completed" but is no longer completed based
--on curator actions in the pub tracker -- aka: pub_history_tracking table.

define status varchar(50);
let status = (Select pts_status from pub_tracking_Status
    	     	     where pts_pk_id = vPubStatusId);

if (status like 'CLOSED%')
  then 
  update publication 
  	 set pub_completion_date = current year to second
	 where zdb_id = vPubZdbId;
 else 
 update publication
	set pub_completion_date = null
	where zdb_id = vPubZdbId;

end if;

end procedure;
