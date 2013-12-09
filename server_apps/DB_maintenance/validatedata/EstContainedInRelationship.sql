unload to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/reportRecords.txt
select mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrkr_type, mrel_type
             from   marker, marker_relationship, marker_relationship_type
             where  mrkr_type = 'EST'
             and    (mrkr_zdb_id = mrel_mrkr_2_zdb_id
                     and    mrel_type = mreltype_name
                     and    mrel_type != 'contains polymorphism'
                     and    mreltype_2_to_1_comments = 'Contained in')
             union
             select mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrkr_type, mrel_type
             from   marker, marker_relationship, marker_relationship_type
             where  mrkr_type = 'EST'
             and    (mrkr_zdb_id = mrel_mrkr_1_zdb_id
                     and    mrel_type = mreltype_name
                     and    mrel_type != 'contains polymorphism'
                     and    mreltype_1_to_2_comments = 'Contains')
