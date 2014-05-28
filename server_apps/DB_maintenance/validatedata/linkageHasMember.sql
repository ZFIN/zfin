unload to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/reportRecords.txt
select lnkg_zdb_id,
                    lnkg_chromosome,
                    recattrib_source_zdb_id
               from linkage, record_attribution
	      where lnkg_zdb_id not in (
	            	      select lnkgmem_linkage_zdb_id 
                                from linkage_member)
                and lnkg_zdb_id = recattrib_data_zdb_id
