select count(*) from db_link where dblink_acc_num ~ 'ENSDARG' AND dblink_linked_recid ~ 'GENE'
                               and not exists (
            select * from transcript, marker_relationship where mrel_mrkr_1_zdb_id = dblink_linked_recid
                                                            AND mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
                                                            --AND tscript_type_id in (1, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 19)
                                                            AND tscript_type_id in (2,5, 16, 17, 18)
        );

select * from db_link where dblink_acc_num ~ 'ENSDARG' AND dblink_linked_recid ~ 'GENE'
                        and not exists (
            select * from transcript, marker_relationship where mrel_mrkr_1_zdb_id = dblink_linked_recid
                                                            AND mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
                                                            --AND tscript_type_id in (1, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 19)
                                                            AND tscript_type_id in (2,5, 16, 17, 18)
        );

select * from db_link where dblink_acc_num ~ 'ENSDARG' AND dblink_linked_recid ~ 'GENE'
                        and not exists (
            select tscript_mrkr_zdb_id as ct from transcript, marker_relationship where mrel_mrkr_1_zdb_id = 'ZDB-GENE-060130-140'
                                                                                    AND mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
                                                                                    --AND tscript_type_id in (1, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 19)
                                                                                    AND tscript_type_id in (2,5, 16, 17, 18) group by tscript_mrkr_zdb_id having count(*) > 0
        );

select * from db_link where dblink_acc_num ~ 'ENSDARG' AND dblink_linked_recid ~ 'GENE'
                        and not exists (
            select tscript_mrkr_zdb_id as ct from transcript, marker_relationship where mrel_mrkr_1_zdb_id = 'ZDB-GENE-060130-140'
                                                                                    AND mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
                                                                                    --AND tscript_type_id in (1, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 19)
                                                                                    AND tscript_type_id in (2,5, 16, 17, 18) group by tscript_mrkr_zdb_id having count(*) > 0
        );

select count(*) from db_link where dblink_acc_num ~ 'ENSDARG' AND dblink_linked_recid ~ 'GENE'
                               and not exists (
            select tscript_mrkr_zdb_id as ct from transcript, marker_relationship where mrel_mrkr_1_zdb_id = 'ZDB-GENE-060130-140'
                                                                                    AND mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
                                                                                    --AND tscript_type_id in (1, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 19)
                                                                                    AND tscript_type_id in (2,5, 16, 17, 18) group by tscript_mrkr_zdb_id having count(*) > 0
        );

