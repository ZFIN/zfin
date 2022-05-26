begin work;

-- creates a table that should match pre_marker_go_term_evidence so we can
-- preserve IDs for duplicates

DROP TABLE IF EXISTS tmp_uniprot_last_run_marker_go_term_evidence;

CREATE TABLE tmp_uniprot_last_run_marker_go_term_evidence AS SELECT DISTINCT
     mgte.mrkrgoev_zdb_id,
     mgte.mrkrgoev_mrkr_zdb_id,
     mgte.mrkrgoev_term_zdb_id,
     mgte.mrkrgoev_source_zdb_id,
     mgte.mrkrgoev_notes,
     igm.infgrmem_inferred_from AS infgrmem_inferred_from
 FROM
     marker_go_term_evidence mgte
         INNER JOIN inference_group_member igm ON mgte.mrkrgoev_zdb_id = igm.infgrmem_mrkrgoev_zdb_id
         AND (infgrmem_inferred_from ILIKE 'UniProtKB-KW:%'
            OR infgrmem_inferred_from ILIKE 'InterPro:%'
            OR infgrmem_inferred_from ILIKE 'EC:%');

--rollback work;
commit work;
