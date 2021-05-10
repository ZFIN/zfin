SELECT lnkg_zdb_id,
       lnkg_chromosome,
       recattrib_source_zdb_id
FROM   linkage,
       record_attribution
WHERE  not exists (select 'x' from linkage_membership 
       	   	  	  where lnkgm_linkage_zdb_id = lnkg_zdb_id)
       AND lnkg_zdb_id = recattrib_data_zdb_id
       AND lnkg_comments != 'ZFIN historical data'
and not exists (Select 'x' from linkage_single
    	       	       where lsingle_lnkg_zdb_id = lnkg_zdb_id)
