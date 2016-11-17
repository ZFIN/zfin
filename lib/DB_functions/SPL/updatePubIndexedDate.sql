create procedure updatePubIndexedDate (vPubZdbId varchar(50),
       		 			 vPubStatusId int8)

--procedure assumes that on insert of pub_tracking_history, only the "Ready for Curation.

define status varchar(50);
let status = (Select pts_status from pub_tracking_Status
    	     	     where pts_pk_id = vPubStatusId);

if (status = 'INDEXED')
  then 
  update publication 
  	 set pub_indexed_date = current year to second
	 where zdb_id = vPubZdbId;
  update publication
  	 set pub_is_indexed = 't'
	 where zdb_id = vPubZdbId;

end if;
end procedure;
