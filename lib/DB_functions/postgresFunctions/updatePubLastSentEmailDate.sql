create or replace function updatePubLastSentEmailDate (vPubZdbId text, vDateReceived timestamp)
returns void as $$

begin
  update publication 
  	 set pub_last_sent_email_date = vDateReceived
	 where zdb_id = vPubZdbId;

end
$$ LANGUAGE plpgsql;
