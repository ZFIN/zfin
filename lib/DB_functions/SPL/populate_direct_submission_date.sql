----------------------------------------------------------------
-- This procedure populates expression_experiment.xpatex_direct_submission_date
-- field for a given XPAT ZDB ID.
--
-- INPUT VARS:
--             xpat zdbId 
--
-- OUTPUT VARS:
--              None
-- EFFECTS:
--              fill xpatex_direct_submission_date field
-------------------------------------------------------------

create procedure populate_direct_submission_date (
				xpatexZdbId   varchar(50))

	update expression_experiment 
           set xpatex_direct_submission_date =  get_date_from_id (xpatexZdbId, "YYYY-MM-DD") :: datetime
 

end procedure;
	 	 