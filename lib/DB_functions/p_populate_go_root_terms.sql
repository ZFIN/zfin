create or replace function p_populate_go_root_terms (mrkrZdbId  text, 
				          mrkrName  text,
				 	  mrkrType  varchar(20))

returns void as $$
begin
	if (mrkrType = 'GENE' AND 
	    (mrkrName like 'zgc:%' OR mrkrName like 'im:%')) then 
 
		select p_insert_marker_go_ev(mrkrZdbId,'ZDB-TERM-091209-4029','ZDB-PUB-031118-1','ND');

		select p_insert_marker_go_ev(mrkrZdbId,'ZDB-TERM-091209-2432','ZDB-PUB-031118-1','ND');

		select p_insert_marker_go_ev(mrkrZdbId,'ZDB-TERM-091209-6070','ZDB-PUB-031118-1','ND');


    end if;

end

$$ LANGUAGE plpgsql
	 	 
