SELECT
    recattrib_pk_id,
    recattrib_data_zdb_id,
    recattrib_source_zdb_id,
    wd_new_zdb_id,
    wd_display_note
FROM
    record_attribution
        LEFT JOIN zdb_active_source zas ON recattrib_source_zdb_id = zas.zactvs_zdb_id
        LEFT JOIN withdrawn_data ON recattrib_source_zdb_id = withdrawn_data.wd_old_zdb_id
WHERE
    zas.zactvs_zdb_id IS NULL
ORDER BY
    recattrib_source_zdb_id DESC,
    wd_new_zdb_id