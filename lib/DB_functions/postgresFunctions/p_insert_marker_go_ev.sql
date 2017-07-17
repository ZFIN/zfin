create or replace function p_insert_marker_go_ev (mrkrZdbId	text, 
				      goTermZdbId	text, 
			                 pubZdbId	text,
	                                   evCode      varchar(3))

returns void as $$

	declare mrkrGOEvZdbId	text) := get_id('MRKRGOEV');
	
	begin 

	insert into zdb_active_data (zactvd_zdb_id ) 
		  	         values (mrkrGOEvZdbId);

	insert into marker_go_term_evidence (
			  mrkrgoev_zdb_id, mrkrgoev_mrkr_zdb_id,
			  mrkrgoev_term_zdb_id, mrkrgoev_source_zdb_id,
			  mrkrgoev_evidence_code, 
			  mrkrgoev_date_entered, mrkrgoev_date_modified,
			  mrkrgoev_annotation_organization)
	         select mrkrGOEvZdbId, mrkrZdbId,
                          goTermZdbId, pubZdbId,
			  evCode, 
                          TODAY, TODAY,
			  mrkrgoevas_pk_id
	             from marker_go_Term_Evidence_annotation_organization
  		     where mrkrgoevas_annotation_organization = 'ZFIN';

end

$$ LANGUAGE plpgsql
