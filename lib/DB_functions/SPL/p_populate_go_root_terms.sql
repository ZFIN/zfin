
---------------------------------------------------------------------------------
-- This procedure is called by marker_insert_trigger. When a new marker is created,
-- in the case that is either a "zgc:" or "im:" gene, three GO root terms would
-- be attached to that gene through this procedure.
---------------------------------------------------------------------------------- 
create procedure p_populate_go_root_terms (mrkrZdbId  varchar(50), 
				          mrkrName  varchar(50),
				 	  mrkrType  varchar(20))


	if (mrkrType = "GENE" AND 
	    (mrkrName like "zgc:%" OR mrkrName like "im:%")) then 
 
		execute procedure p_insert_marker_go_ev(mrkrZdbId,"ZDB-GOTERM-031121-2395","ZDB-PUB-031118-1","ND");

		execute procedure p_insert_marker_go_ev(mrkrZdbId,"ZDB-GOTERM-031121-846","ZDB-PUB-031118-1","ND");

		execute procedure p_insert_marker_go_ev(mrkrZdbId,"ZDB-GOTERM-031121-4370","ZDB-PUB-031118-1","ND");


    end if; 

end procedure;
	 	 