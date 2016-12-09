create procedure updatePubLastSentEmailDate (vPubZdbId varchar(50), vDateReceived datetime year to day )


  update publication 
  	 set pub_last_sent_email_date = vDateReceived
	 where zdb_id = vPubZdbId;
end procedure;
