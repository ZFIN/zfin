SELECT mrkr_zdb_id,
  mrkr_abbrev,
  ra.recattrib_source_zdb_id
FROM   marker,
  record_attribution AS ra,
  curation
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   marker_go_term_evidence,
                     term
                   WHERE  mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
                          AND mrkrgoev_term_zdb_id = term_zdb_id
                          AND term_ontology IN ( 'biological_process',
                                                 'molecular_function'
                   )
                          AND mrkrgoev_evidence_code IN (
                     'IDA', 'IMP', 'IGI', 'IPI' ))
       AND EXISTS((SELECT 'x'
                   FROM   marker_relationship
                   WHERE  Get_obj_type(mrel_mrkr_1_zdb_id) IN (
                     'TALEN', 'CRISPR', 'MRPHLNO' )
                          AND mrel_mrkr_2_zdb_id = mrkr_zdb_id
                          AND EXISTS(SELECT 'z'
                                     FROM   record_attribution AS rec
                                     WHERE  rec.recattrib_source_zdb_id =
                                            ra.recattrib_source_zdb_id
                                            AND rec.recattrib_data_zdb_id =
                                                mrel_mrkr_1_zdb_id))
                  UNION
                  (SELECT 'y'
                   FROM   feature_marker_relationship,
                     feature
                   WHERE  fmrel_type = 'is allele of'
                          AND fmrel_mrkr_zdb_id = mrkr_zdb_id
                          AND fmrel_ftr_zdb_id = feature_zdb_id
                          AND substring(feature_abbrev from 1 for 2) != 'sa'
AND substring(feature_abbrev from 1 for 2) != 'la'
AND EXISTS(SELECT 'z'
FROM   record_attribution AS rec
WHERE  rec.recattrib_source_zdb_id =
ra.recattrib_source_zdb_id
AND rec.recattrib_data_zdb_id =
feature_zdb_id)
))
AND ra.recattrib_data_zdb_id = mrkr_zdb_id
AND cur_pub_zdb_id = ra.recattrib_source_zdb_id
AND cur_closed_date IS NULL
AND cur_topic = 'GO'