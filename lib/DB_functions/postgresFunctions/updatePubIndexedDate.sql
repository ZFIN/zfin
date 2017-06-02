create or replace function updatePubIndexedDate (vPubZdbId varchar(50),
       		 			 vPubStatusId int8)
returns void as $$

--procedure assumes that on insert of pub_tracking_history, only the "Ready for Curation.

declare status varchar(50) := (Select pts_status from pub_tracking_Status
    	     	     where pts_pk_id = vPubStatusId);

begin
if (status = 'INDEXED')
  then 
  update publication 
  	 set pub_indexed_date = now()
	 where zdb_id = vPubZdbId;
  update publication
  	 set pub_is_indexed = 't'
	 where zdb_id = vPubZdbId;

end if;
end

$$ LANGUAGE plpgsql;
