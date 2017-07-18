create or replace function checkDblinkTranscriptWithdrawn (vDblinkZdbId text,
       		 				      vTscriptZdbId text,
						      vFDBContZdbId text
)
returns void as $$
begin
        if ( vFDBContZdbId = "ZDB-FDBCONT-100114-1")        
	then   	
		
		if (not exists (Select 'x' from transcript
		   	       	       where vTscriptZdbId = tscript_mrkr_zdb_id
		   	       	         and tscript_status_id = 1) )
		
		then
	   		raise exception 'FAIL!: Vega Withdrawn can only be put on Withdrawn Transcripts.';
		
		end if ;
	
	end if ;
end
$$ LANGUAGE plpgsql
