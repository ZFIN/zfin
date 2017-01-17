create trigger pub_correspondence_sent_tracker_insert_trigger
 insert on pub_correspondence_sent_tracker
 referencing new as newst
 for each row (execute procedure updatePubLastSentEmailDate(newst.pubcst_pub_zdb_id,newst.pubcst_date_sent))

