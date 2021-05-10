delete from record_attribution
  where recattrib_source_zdb_id = 'ZDB-PUB-110127-1'
  and recattrib_data_zdb_id like 'ZDB-GENE%'
  and not exists (select 'x' from marker_go_term_evidence
                  where mrkrgoev_mrkr_zdb_id = recattrib_data_zdb_id);
