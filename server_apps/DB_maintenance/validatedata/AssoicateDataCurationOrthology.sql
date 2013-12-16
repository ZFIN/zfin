unload to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/reportRecords.txt
select recattrib_data_Zdb_id
             from   record_attribution
             where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
             and get_obj_type(recattrib_data_zdb_id) not in ('DBLINK','MREL','MRKRGOEV','DALIAS','ORTHO')
             and    recattrib_data_Zdb_id not in (
             select oevdisp_gene_zdb_id
             from   orthologue_evidence_display
             where  exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_gene_zdb_id = recattrib_data_Zdb_id
                    )
             and    exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_zdb_id = recattrib_data_Zdb_id
                    )
             union
             select oevdisp_zdb_id
             from   orthologue_evidence_display
             where  exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_gene_zdb_id = recattrib_data_Zdb_id
                    )
             and    exists (
                       select recattrib_data_Zdb_id
                       from   record_attribution
                       where  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                       and    oevdisp_zdb_id = recattrib_data_Zdb_id
                    )
             );
