create procedure p_insert_into_record_attribution (vTableZdbId varchar(50), 
					vSourceZdbId varchar(50),
					vDataZdbId varchar(50))
--p_insert_into_record_attribution.
-----------------------------------
--table_zdb_ids and data_zdb_ids should be attributed to pubs.
--table_zdb_ids are specific ids generated on entry to tables (like
--mrkrgoev_zdb_id in marker_go_Term_evidence, or xpatex_zdb_id
--in expression_experiment.)
--data zdb_ids are actual data values in those tables (like 
--mrkrgoev_mrkr_zdb_id in marker_Go_Term_evidence, and 
--xpatex_probe_zdb_id, xpat_gene_zdb_id in expression_experment).  
--These two tables in particular, have 'source' in each record.  
--These sources should also be found in record_attribution.
--this procedure inserts 1 record into record attribution 
--for every new table_zdb_id entered, and one into record_attribution
--for every new combo of data_zdb_id and source if the data value is not null. 


	define vOk_tableId integer ;
	define vOk_dataId integer ;
	define vCuratorPub integer ;

	let vOk_tableId = 0 ;
	let vOk_dataId = 0 ;
	let vCuratorPub = 0 ;	

		let vOk_tableId = (select count(*)
			     		from record_attribution
			     		where recattrib_data_zdb_id = 
								vTableZdbId
		    			and recattrib_source_zdb_id = 
								vSourceZdbId) ;
		if vOk_tableId > 0

		then
	 	 let vOk_tableId = 1  ;

		else
	   	
	  	insert into record_attribution (recattrib_data_zdb_id,
						recattrib_source_zdb_id)
			values (vTableZdbId, vSourceZdbId) ;

		end if ;	

	if vDataZdbId is not null 
	
	then

		let vOk_dataId = (select count(*)
					from record_attribution
					where recattrib_data_zdb_id = 
						vDataZdbId
					and recattrib_source_zdb_id = 
						vSourceZdbId
		 		) ;

	 	let vCuratorPub = (select count(*) 
					from publication
					where zdb_id = vSourceZdbId  
					and zdb_id in ('ZDB-PUB-020723-1',
							'ZDB-PUB-040330-1',
                            				'ZDB-PUB-031118-3',
							'ZDB-PUB-020724-1')
				) ;


		if vOk_dataId > 0

		then
	
		   let vOk_dataId = 1 ;

        	elif 

	  	vCuratorPub > 0

	  	then let vOk_dataId = 1 ;
	
		else
			insert into record_attribution (recattrib_data_zdb_id,
						recattrib_source_zdb_id)
				values (vDataZdbId, vSourceZdbId) ;
 
		end if ;   
	end if ;

end procedure ;
