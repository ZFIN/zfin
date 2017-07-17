create or replace function p_insert_into_record_attribution_tablezdbids 
					(vTableZdbId text), 
					vSourceZdbId text))

returns void as $$
--p_insert_into_record_attribution_tablezdbids.
-----------------------------------
--table_zdb_ids should slways be attributed to pubs.
--table_zdb_ids are specific ids generated on entry to tables (like
--mrkrgoev_zdb_id in marker_go_Term_evidence, or xpatex_zdb_id
--in expression_experiment.)
--  
--These mrkrgoev, environment, xpatex 
--in particular, have 'source' in each record.  
--These sources should also be found in record_attribution.
--this procedure inserts 1 record into record attribution 
--for every new table_zdb_id entered 


	declare vOk_tableId integer :=0;
	 vCuratorPub integer :=0;
begin
	vOk_tableId = (select count(*)
			     		from record_attribution
			     		where recattrib_data_zdb_id = 
								vTableZdbId
		    			and recattrib_source_zdb_id = 
								vSourceZdbId) ;
		if vOk_tableId > 0

		then
	 	 vOk_tableId = 1  ;

		else
	   	
	  	insert into record_attribution (recattrib_data_zdb_id,
						recattrib_source_zdb_id)
			values (vTableZdbId, vSourceZdbId) ;

		end if ;	

end

$$ LANGUAGE plpgsql
