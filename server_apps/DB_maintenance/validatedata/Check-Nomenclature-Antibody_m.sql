-- create temp table of all genes and pseudo genes that have updated symbols within
-- the last 31 days
CREATE TEMP TABLE gene_names_changed_temp as
SELECT   rec_id,
         old_value,
         new_value,
         upd_when
FROM     updates
WHERE    upd_when > now() - interval '31 days'
AND      Get_obj_type(rec_id) IN ('GENE',
                                  'GENEP')
AND      field_name ='mrkr_abbrev'
ORDER BY upd_when DESC;


SELECT b.mrkr_zdb_id,
       up.old_value,
       b.mrkr_abbrev        AS targetGene,
       antibody.mrkr_abbrev AS antibodyName,
       antibody.mrkr_zdb_id AS antibodyID,
       upd_when AS dateNameChange
FROM   marker AS antibody,
       marker b,
       marker_relationship mrel,
       gene_names_changed_temp up
WHERE  antibody.mrkr_type = 'ATB'
       AND b.mrkr_zdb_id = rec_id
       AND mrel.mrel_mrkr_2_zdb_id = antibody.mrkr_zdb_id
       AND mrel.mrel_mrkr_1_zdb_id = b.mrkr_zdb_id
       AND up.old_value LIKE Substring(antibody.mrkr_abbrev FROM
                                       position('-' in antibody.mrkr_abbrev) + 1
                                       FOR 20)|| '%';
