SELECT DISTINCT o.dblink_linked_recid,
                tscripts_status,
                g.geno_display_name
FROM   db_link AS o,
       transcript,
       transcript_status,
       genotype g,
       probe_library pl,
       marker_relationship mr,
       clone c
WHERE  o.dblink_acc_num LIKE 'OTTDART%'
  AND o.dblink_linked_recid LIKE 'ZDB-TSCRIPT%'
  AND NOT EXISTS (SELECT *
                  FROM   db_link AS e
                  WHERE  e.dblink_acc_num LIKE 'ENSDART%'
                    AND e.dblink_linked_recid = o.dblink_linked_recid)
  AND tscript_status_id != 1
  AND tscript_mrkr_zdb_id = o.dblink_linked_recid
  AND tscript_status_id = tscripts_pk_id
  AND pl.probelib_zdb_id = c.clone_probelib_zdb_id
  AND g.geno_zdb_id = pl.probelib_strain_zdb_id
  AND mr.mrel_mrkr_1_zdb_id = c.clone_mrkr_zdb_id
  AND mr.mrel_mrkr_2_zdb_id = tscript_mrkr_zdb_id
  AND mr.mrel_type = 'clone contains transcript'
  AND g.geno_nickname = 'TU';