unload to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/reportRecords.txt
select xpatex_zdb_id, xpatex_probe_feature_zdb_id, xpatex_gene_zdb_id
               from expression_experiment
               where xpatex_probe_feature_zdb_id is not null
               and not exists
                (
                   select * 
                     from marker_relationship
                     where xpatex_probe_feature_zdb_id = mrel_mrkr_2_zdb_id
                     and xpatex_gene_zdb_id = mrel_mrkr_1_zdb_id
                 )
