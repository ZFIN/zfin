unload to STR-Report
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
       AND b.mrkr_abbrev != ( Substring(a.mrkr_abbrev FROM
( Length(a.mrkr_abbrev) - Length(b.mrkr_abbrev) + 1 ) FOR
(
Length(b.mrkr_abbrev) )) )
ORDER  BY b.mrkr_abbrev;

-- create temp table of all genes and pseudo genes that have updated symbols within
-- the last 31 days
SELECT   rec_id,
         old_value,
         new_value,
         when
FROM     updates
WHERE    when > today -31
AND      Get_obj_type(rec_id) IN ('GENE',
                                  'GENEP')
AND      field_name ='mrkr_abbrev'
ORDER BY when DESC
INTO temp gene_names_changed_temp;


unload to Antibody-Report
SELECT b.mrkr_zdb_id,
       up.old_value,
       b.mrkr_abbrev        AS targetGene,
       antibody.mrkr_abbrev AS antibodyName,
       antibody.mrkr_zdb_id AS antibodyID,
       when AS dateNameChange
FROM   marker AS antibody,
       marker b,
       gene_names_changed_temp up
WHERE  antibody.mrkr_type = 'ATB'
       AND b.mrkr_zdb_id = rec_id
       AND up.old_value LIKE Substring(antibody.mrkr_abbrev FROM
                                       Charindex('-', antibody.mrkr_abbrev) + 1
                                       FOR 20)|| '%';