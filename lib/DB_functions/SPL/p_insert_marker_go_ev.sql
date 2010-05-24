------------------------------------------------------------------------
-- This procedure accepts a marker zdb id, GO term zdb id, pub zdb id
-- and evidence code, then generates a new marker go term evidence record,
-- attributing to the "ZFIN auto assignment".
-----------------------------------------------------------------------

create procedure p_insert_marker_go_ev (mrkrZdbId	varchar(50), 
				      goTermZdbId	varchar(50), 
			                 pubZdbId	varchar(50),
	                                   evCode      varchar(3))

	define mrkrGOEvZdbId	varchar(50);

	let mrkrGOEvZdbId = get_id("MRKRGOEV");
	
	insert into zdb_active_data (zactvd_zdb_id ) 
		  	         values (mrkrGOEvZdbId);

	insert into marker_go_term_evidence (
			  mrkrgoev_zdb_id, mrkrgoev_mrkr_zdb_id,
			  mrkrgoev_term_zdb_id, mrkrgoev_source_zdb_id,
			  mrkrgoev_evidence_code, 
			  mrkrgoev_date_entered, mrkrgoev_date_modified,
                          mrkrgoev_contributed_by, mrkrgoev_modified_by)
	          values (mrkrGOEvZdbId, mrkrZdbId,
                          goTermZdbId, pubZdbId,
			  evCode, 
                          TODAY, TODAY,
                          "ZFIN auto assignment", "ZFIN auto assignment");



end procedure;
