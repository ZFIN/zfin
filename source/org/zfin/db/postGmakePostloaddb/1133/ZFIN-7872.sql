
SELECT count(t.mrkr_zdb_id) as "Number of Transcripts with ENSDART records associated"
FROM   marker AS t,
       marker AS g,
       db_link,
       marker_relationship
WHERE  dblink_acc_num ~ 'ENSDART'
  AND dblink_linked_recid = t.mrkr_zdb_id
  AND mrel_mrkr_2_zdb_id = t.mrkr_zdb_id
  AND mrel_mrkr_1_zdb_id = g.mrkr_zdb_id
  AND g.mrkr_type = 'GENE';

SELECT count(t.mrkr_zdb_id) as "Number of Transcripts with ENSDART records associated where transcript name does not match gene name"
FROM   marker AS t,
       marker AS g,
       db_link,
       marker_relationship
WHERE  dblink_acc_num ~ 'ENSDART'
  AND dblink_linked_recid = t.mrkr_zdb_id
  AND mrel_mrkr_2_zdb_id = t.mrkr_zdb_id
  AND mrel_mrkr_1_zdb_id = g.mrkr_zdb_id
  AND g.mrkr_type = 'GENE'
  AND g.mrkr_abbrev != Left(t.mrkr_abbrev, -4);

SELECT t.mrkr_zdb_id as "Transcript ID",
       t.mrkr_abbrev as "Transcript symbol",
       Left(t.mrkr_abbrev, -4) as "transcript minus appendix" ,
       g.mrkr_abbrev as "gene symbol",
       mrel_mrkr_1_zdb_id as "Gene ZDB ID"
FROM   marker AS t,
       marker AS g,
       db_link,
       marker_relationship
WHERE  dblink_acc_num ~ 'ENSDART'
  AND dblink_linked_recid = t.mrkr_zdb_id
  AND mrel_mrkr_2_zdb_id = t.mrkr_zdb_id
  AND mrel_mrkr_1_zdb_id = g.mrkr_zdb_id
  AND g.mrkr_type = 'GENE'
  AND g.mrkr_abbrev != Left(t.mrkr_abbrev, -4);


SELECT t.mrkr_zdb_id as "Transcript ID",
       t.mrkr_abbrev as "Transcript symbol",
       Left(t.mrkr_abbrev, -4) as "transcript minus appendix" ,
       g.mrkr_abbrev as "gene symbol",
       mrel_mrkr_1_zdb_id as "Gene ZDB ID"
FROM   marker AS t,
       marker AS g,
       db_link,
       marker_relationship
WHERE  dblink_acc_num ~ 'ENSDART'
  AND dblink_linked_recid = t.mrkr_zdb_id
  AND mrel_mrkr_2_zdb_id = t.mrkr_zdb_id
  AND mrel_mrkr_1_zdb_id = g.mrkr_zdb_id
  AND g.mrkr_type = 'GENE'
  AND NOT ((g.mrkr_abbrev = Left(t.mrkr_abbrev, -4)) OR
    exists (select * from data_alias where dalias_data_zdb_id = g.mrkr_zdb_id AND dalias_alias = Left(t.mrkr_abbrev, -4)))
;

SELECT count(t.mrkr_zdb_id)
FROM   marker AS t,
       marker AS g,
       db_link,
       marker_relationship
WHERE  dblink_acc_num ~ 'ENSDART'
  AND dblink_linked_recid = t.mrkr_zdb_id
  AND mrel_mrkr_2_zdb_id = t.mrkr_zdb_id
  AND mrel_mrkr_1_zdb_id = g.mrkr_zdb_id
  AND g.mrkr_type = 'GENE'
  AND NOT ((g.mrkr_abbrev = Left(t.mrkr_abbrev, -4)) OR
    exists (select * from data_alias where dalias_data_zdb_id = g.mrkr_zdb_id AND dalias_alias = Left(t.mrkr_abbrev, -4)))
;

-- 1072 transcripts that do not match associated gene or a previous name


SELECT t.mrkr_zdb_id as "Transcript ID",
       t.mrkr_abbrev as "Transcript symbol",
       g.mrkr_abbrev as "gene symbol",
       mrel_mrkr_1_zdb_id as "Gene ZDB ID"
FROM   marker AS t,
       marker AS g,
       db_link,
       marker_relationship
WHERE  dblink_acc_num ~ 'ENSDART'
  AND dblink_linked_recid = t.mrkr_zdb_id
  AND mrel_mrkr_2_zdb_id = t.mrkr_zdb_id
  AND mrel_mrkr_1_zdb_id = g.mrkr_zdb_id
  AND g.mrkr_type = 'GENE'
  AND NOT ((g.mrkr_abbrev = Left(t.mrkr_abbrev, -4)) OR
           exists (select * from data_alias where dalias_data_zdb_id = g.mrkr_zdb_id AND dalias_alias = Left(t.mrkr_abbrev, -4)))
;


