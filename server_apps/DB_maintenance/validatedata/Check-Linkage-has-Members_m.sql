SELECT lnkg_zdb_id,
       lnkg_chromosome,
       recattrib_source_zdb_id
FROM   linkage,
       record_attribution
WHERE  lnkg_zdb_id NOT IN (SELECT lnkgmem_linkage_zdb_id
                           FROM   linkage_member)
       AND lnkg_zdb_id = recattrib_data_zdb_id
       AND lnkg_comments != 'ZFIN historical data'