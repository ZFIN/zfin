create or replace function updatePubLastCorrespondenceDate (vPubZdbId text, vDateReceived timestamp)
returns void as $$

begin
  update publication 
  	 set pub_last_correspondence_date = vDateReceived
	 where zdb_id = vPubZdbId;

end
$$ LANGUAGE plpgsql;
