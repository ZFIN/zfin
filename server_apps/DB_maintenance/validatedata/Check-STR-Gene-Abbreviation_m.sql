SELECT a.mrkr_abbrev,
       b.mrkr_abbrev
FROM   marker a,
       marker b,
       marker_relationship c
WHERE  a.mrkr_zdb_id = c.mrel_mrkr_1_zdb_id
       AND b.mrkr_zdb_id = c.mrel_mrkr_2_zdb_id
       AND EXISTS (SELECT 'x'
                   FROM   marker_type_group_member
                   WHERE  a.mrkr_type = mtgrpmem_mrkr_type
                          AND mtgrpmem_mrkr_type_group = 'KNOCKDOWN_REAGENT')
       AND NOT EXISTS (SELECT 'x'
                       FROM   marker_relationship d
                       WHERE  d.mrel_mrkr_1_zdb_id = c.mrel_mrkr_1_zdb_id
                              AND d.mrel_mrkr_2_zdb_id != c.mrel_mrkr_2_zdb_id)
       AND b.mrkr_abbrev != ( Substring(a.mrkr_abbrev FROM
( Length(a.mrkr_abbrev) - Length(b.mrkr_abbrev) + 1 ) FOR
(
Length(b.mrkr_abbrev) )) )
ORDER  BY b.mrkr_abbrev;