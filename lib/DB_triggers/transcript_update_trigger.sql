create trigger transcript_update_trigger update of 
       tscript_mrkr_Zdb_id, tscript_load_id    
    on transcript referencing old as oldt new 
    as newt
    for each row
        (
        execute function setTscriptLoadId(newt.tscript_mrkr_zdb_id, 
			 		newt.tscript_load_id ) 
	    into transcript.tscript_load_id
);
