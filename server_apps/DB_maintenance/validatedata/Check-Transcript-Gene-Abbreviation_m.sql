SELECT a.mrkr_abbrev,
       b.mrkr_abbrev
FROM   marker a,
       marker b,
       marker_relationship c,transcript e
WHERE  a.mrkr_zdb_id = c.mrel_mrkr_2_zdb_id
       AND b.mrkr_zdb_id = c.mrel_mrkr_1_zdb_id
       AND EXISTS (SELECT 'x'
                   FROM   marker_type_group_member
                   WHERE  a.mrkr_type = mtgrpmem_mrkr_type
                          AND mtgrpmem_mrkr_type_group = 'TRANSCRIPT')
       AND NOT EXISTS (SELECT 'x'
                       FROM   marker_relationship d
                       WHERE  d.mrel_mrkr_2_zdb_id = c.mrel_mrkr_2_zdb_id
                              AND d.mrel_mrkr_1_zdb_id != c.mrel_mrkr_1_zdb_id)
       AND b.mrkr_abbrev != (Substring(a.mrkr_abbrev ,0,position('-' in a.mrkr_abbrev)))
and b.mrkr_abbrev not like '%-%'
and a.mrkr_zdb_id=e.tscript_mrkr_zdb_id
and e.tscript_status_id!=1
union
SELECT a.mrkr_abbrev,
       b.mrkr_abbrev
FROM   marker a,
       marker b,
       marker_relationship c,transcript e

WHERE  a.mrkr_zdb_id = c.mrel_mrkr_2_zdb_id
       AND b.mrkr_zdb_id = c.mrel_mrkr_1_zdb_id
       AND EXISTS (SELECT 'x'
                   FROM   marker_type_group_member
                   WHERE  a.mrkr_type = mtgrpmem_mrkr_type
                          AND mtgrpmem_mrkr_type_group = 'TRANSCRIPT')
       AND NOT EXISTS (SELECT 'x'
                       FROM   marker_relationship d
                       WHERE  d.mrel_mrkr_2_zdb_id = c.mrel_mrkr_2_zdb_id
                              AND d.mrel_mrkr_1_zdb_id != c.mrel_mrkr_1_zdb_id)
       AND (b.mrkr_abbrev != (Substring(a.mrkr_abbrev ,0,position('-0' in a.mrkr_abbrev))) and (b.mrkr_abbrev != (Substring(a.mrkr_abbrev ,0,position('-2' in a.mrkr_abbrev)))))
and b.mrkr_abbrev like '%-%'
and a.mrkr_zdb_id=e.tscript_mrkr_zdb_id
and (e.tscript_status_id is null or e.tscript_status_id!=1) ;
