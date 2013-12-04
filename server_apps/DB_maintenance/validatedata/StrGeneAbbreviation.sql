unload to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/reportRecords.txt
select a.mrkr_abbrev, b.mrkr_abbrev
               from marker a, marker b, marker_relationship c
               where a.mrkr_zdb_id = c.mrel_mrkr_1_zdb_id
               and b.mrkr_zdb_id = c.mrel_mrkr_2_zdb_id
               and b.mrkr_abbrev not like 'mir%'
               and exists (Select 'x' from marker_type_group_member where
                              a.mrkr_type = mtgrpmem_mrkr_type
                              and mtgrpmem_mrkr_type_group = 'KNOCKDOWN_REAGENT')
               and not exists (select 'x' from marker_relationship d
                                 where d.mrel_mrkr_1_zdb_id = c.mrel_mrkr_1_zdb_id
                                 and d.mrel_mrkr_2_zdb_id != c.mrel_mrkr_2_zdb_id)
                and b.mrkr_abbrev !=
               (substring(a.mrkr_abbrev
                            from
                             (length(a.mrkr_abbrev)-length(b.mrkr_abbrev)+1)
                            for
                             (length(b.mrkr_abbrev))
                          )
                )
              order by b.mrkr_abbrev