create or replace function increment_candidate_occurrences (vCndZdbId varchar(50))
        returns int as $counter$

       declare counter  run_candidate.runcan_occurrence_order%TYPE := (select cnd_run_count
       	   	     from candidate 
		     where cnd_zdb_id = vCndZdbId); 
begin
        counter = counter + 1;

       update candidate
         set cnd_run_count = cnd_run_count + 1
         where cnd_zdb_id = vCndZdbId ;

return counter;
end
$counter$ LANGUAGE plpgsql
