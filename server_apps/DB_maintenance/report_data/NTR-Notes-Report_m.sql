SELECT mrkr_zdb_id,
       mrkr_abbrev,
       mrkr_comments,
       dnote_text
FROM   marker
       LEFT OUTER JOIN data_note
                    ON dnote_data_zdb_id = mrkr_zdb_id
WHERE  EXISTS (SELECT *
               FROM   marker_type_group_member
               WHERE  mrkr_type = mtgrpmem_mrkr_type
                      AND mtgrpmem_mrkr_type_group = 'NONTSCRBD_REGION');