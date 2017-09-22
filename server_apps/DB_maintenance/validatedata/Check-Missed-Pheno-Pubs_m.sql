select zdb_id, pub_arrival_date, b.mrkr_zdb_id, b.mrkr_abbrev, a.mrkr_zdb_id, a.mrkr_abbrev
      from publication,record_attribution,
                        marker a, marker b,marker_relationship,marker_type_group_member
            where recattrib_source_zdb_id = zdb_id
        and recattrib_data_zdb_id = a.mrkr_zdb_id
        and  a.mrkr_type = mtgrpmem_mrkr_type
                          AND mtgrpmem_mrkr_type_group = 'KNOCKDOWN_REAGENT'

        and a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
    and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
        and mrel_type = 'knockdown reagent targets gene'
        and mrel_mrkr_2_zdb_id not in (select fmrel_mrkr_Zdb_id from
                    feature_marker_relationship)
        and a.mrkr_zdb_id not in (select fishstr_str_zdb_id from fish_str)
       AND to_char(pub_arrival_date,'YYY-MM-DD') >= '2009-01-01'
and pub_completion_date is null
and not exists (Select 'x' from data_reporting
    	       	       where dr_data_zdb_id = zdb_id)
;

INSERT INTO data_reporting
            (
                        dr_data_zdb_id,
                        dr_report_generated_date,
                        dr_report_name
            )
SELECT DISTINCT zdb_id,
                CURRENT  year to second,
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
AND to_char(pub_arrival_date,'YYY-MM-DD') >= '2009-01-01'
AND             pub_completion_date IS NULL
AND             NOT EXISTS
                (
                       SELECT 'x'
                       FROM   data_reporting
                       WHERE  dr_data_zdb_id = zdb_id); 