create function increment_candidate_occurrences (vCndZdbId varchar(50))
        returning int;

       define counter like run_candidate.runcan_occurrence_order;

       let counter = (select cnd_run_count
       	   	     from candidate 
		     where cnd_zdb_id = vCndZdbId); 

       let counter = counter + 1;

       update candidate
         set cnd_run_count = cnd_run_count + 1
         where cnd_zdb_id = vCndZdbId ;

return counter;
end function;
