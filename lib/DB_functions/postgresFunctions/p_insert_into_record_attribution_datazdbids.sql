create or replace function p_insert_into_record_attribution_datazdbids 
					(vDataZdbId varchar(50), 
					vSourceZdbId varchar(50))
returns void as $$

--p_insert_into_record_attribution_datazdbids.
-----------------------------------
--data zdb_ids are actual data values in tables (like 
--mrkrgoev_mrkr_zdb_id in marker_Go_Term_evidence, and 
--xpatex_probe_zdb_id, xpat_gene_zdb_id in expression_experment).  
--These two tables in particular, have 'source' in each record.  
--These sources should also be found in record_attribution.
--this procedure inserts 1 record into record attribution 
--for every new combo of data_zdb_id and source if the data value is not null
--and are not being attributed to a direct_data_submission pub. 


	declare vOk_dataId integer :=0;
	 vCuratorPub integer :=0;
	 vOk_sourceId integer :=0;

	begin

	vOk_sourceId = ( select count(*) 
	                     from zdb_active_source
	                     where zactvs_zdb_id = vSourceZdbId );

	if ( vDataZdbId is not null and vOk_sourceId == 1)
	
	then

		vOk_dataId = (select count(*)
					from record_attribution
					where recattrib_data_zdb_id = 
						vDataZdbId
					and recattrib_source_zdb_id = 
						vSourceZdbId
		 		) ;

	 	vCuratorPub = (select count(*) 
					from publication
					where zdb_id = vSourceZdbId  
					and zdb_id in ('ZDB-PUB-020723-1',
							'ZDB-PUB-040330-1',
                            				'ZDB-PUB-031118-3',
							'ZDB-PUB-020724-1')
				) ;


		if vOk_dataId > 0

		then
	
		   vOk_dataId = 1 ;

        	elsif 

	  	vCuratorPub > 0

	  	then vOk_dataId = 1 ;
	
		else
			insert into record_attribution (recattrib_data_zdb_id,
						recattrib_source_zdb_id)
				values (vDataZdbId, vSourceZdbId) ;
 
		end if ;   
	end if ;

end
$$ LANGUAGE plpgsql
