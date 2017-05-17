SELECT tscript_mrkr_zdb_id, 
       mrkr_abbrev, 
       Count(*) 
FROM   marker_relationship mrel1,
       marker,
       transcript, 
       transcript_type 
WHERE  tscriptt_pk_id = tscript_type_id 
       AND tscriptt_type != "miRNA"
       AND tscript_mrkr_zdb_id = mrel1.mrel_mrkr_2_zdb_id 
       AND (mrel1.mrel_mrkr_1_zdb_id LIKE "ZDB-GENE%"  OR mrel1.mrel_mrkr_1_zdb_id LIKE "%RNAG%")
       AND EXISTS (SELECT "x" 
                   FROM   marker_relationship mrel2 
                   WHERE  mrel2.mrel_mrkr_1_zdb_id != mrel1.mrel_mrkr_1_zdb_id 
                          AND (mrel2.mrel_mrkr_1_zdb_id LIKE "ZDB-GENE%" OR mrel1.mrel_mrkr_1_zdb_id LIKE "%RNAG%")
                          AND mrel2.mrel_mrkr_2_zdb_id = 
                              mrel1.mrel_mrkr_2_zdb_id) 
       AND mrkr_zdb_id = tscript_mrkr_zdb_id 
GROUP  BY tscript_mrkr_zdb_id, 
          mrkr_abbrev 
HAVING Count(*) > 1;