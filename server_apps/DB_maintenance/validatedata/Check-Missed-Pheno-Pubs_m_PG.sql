SELECT zdb_id,
       pub_arrival_date,
       b.mrkr_zdb_id,
       b.mrkr_abbrev,
       a.mrkr_zdb_id,
       a.mrkr_abbrev
FROM   publication,
       record_attribution,
       marker a,
       marker b,
       marker_relationship,
       marker_type_group_member
WHERE  recattrib_source_zdb_id = zdb_id
       AND recattrib_data_zdb_id = a.mrkr_zdb_id
       AND a.mrkr_type = mtgrpmem_mrkr_type
       AND mtgrpmem_mrkr_type_group = 'KNOCKDOWN_REAGENT'
       AND a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
       AND b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
       AND mrel_type = 'knockdown reagent targets gene'
       AND mrel_mrkr_2_zdb_id NOT IN (SELECT fmrel_mrkr_zdb_id
                                      FROM   feature_marker_relationship)
       AND a.mrkr_zdb_id NOT IN (SELECT fishstr_str_zdb_id
                                 FROM   fish_str)
       AND pub_arrival_date >= '2009-01-01 00:00:00.000'
       AND pub_completion_date IS NULL
       AND NOT EXISTS (SELECT 'x'
                       FROM   data_reporting
                       WHERE  dr_data_zdb_id = zdb_id) 
;

INSERT INTO data_reporting
            (
                        dr_data_zdb_id,
                        dr_report_generated_date,
                        dr_report_name
            )
SELECT DISTINCT zdb_id,
                to_Char(now(), 'YYYY-MM-DD HH:mm:ss' ),
                'Check-Missed-Pheno-Pubs'
FROM            publication,
                record_attribution,
                marker a,
                marker b,
                marker_relationship,
                marker_type_group_member
WHERE           recattrib_source_zdb_id = zdb_id
AND             recattrib_data_zdb_id = a.mrkr_zdb_id
AND             a.mrkr_type = mtgrpmem_mrkr_type
AND             mtgrpmem_mrkr_type_group = 'KNOCKDOWN_REAGENT'
AND             a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
AND             b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
AND             mrel_type = 'knockdown reagent targets gene'
AND             mrel_mrkr_2_zdb_id NOT IN
                (
                       SELECT fmrel_mrkr_zdb_id
                       FROM   feature_marker_relationship)
AND             a.mrkr_zdb_id NOT IN
                (
                       SELECT fishstr_str_zdb_id
                       FROM   fish_str)
AND             pub_arrival_date >='2009-01-01'
AND             pub_completion_date IS NULL
AND             NOT EXISTS
                (
                       SELECT 'x'
                       FROM   data_reporting
                       WHERE  dr_data_zdb_id = zdb_id); 