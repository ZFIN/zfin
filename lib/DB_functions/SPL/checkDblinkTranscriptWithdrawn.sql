create procedure checkDblinkTranscriptWithdrawn (vDblinkZdbId varchar(50),
       		 				      vTscriptZdbId varchar(50),
						      vFDBContZdbId varchar(50)
)
        if ( vFDBContZdbId == "ZDB-FDBCONT-100114-1")        
	then   	
		
		if (not exists (Select 'x' from transcript
		   	       	       where vTscriptZdbId = tscript_mrkr_zdb_id
		   	       	         and tscript_status_id == 1) )
		
		then
	   		raise exception -746,0,"FAIL!: Vega Withdrawn can only be put on Withdrawn Transcripts.";
		
		end if ;
	
	end if ;

end procedure;
