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

unload to Antibody-Report
SELECT antibody.mrkr_abbrev AS antibodyName,
       b.mrkr_abbrev        AS targetGene
FROM   marker AS antibody,
       marker b
WHERE  antibody.mrkr_type = 'ATB'
       AND EXISTS (SELECT 'x'
                   FROM   expression_experiment
                   WHERE  xpatex_atb_zdb_id = antibody.mrkr_zdb_id
                          AND xpatex_gene_zdb_id = b.mrkr_zdb_id)
       AND b.mrkr_abbrev != (  Substring( antibody.mrkr_abbrev FROM (
                                              Length(antibody.mrkr_abbrev) - Length(b.mrkr_abbrev) + 1 )
                                          FOR ( Length(b.mrkr_abbrev) )
                                        )
                             )
ORDER  BY b.mrkr_abbrev;

unload to Antibody-NoGene-Report

SELECT distinct antibody.mrkr_abbrev AS antibodyName
FROM   marker AS antibody
WHERE  not exists (select 'x' from  marker as g
						where g.mrkr_type = 'GENE'
							  and	g.mrkr_abbrev = Substring( antibody.mrkr_abbrev FROM (
                                              Length(antibody.mrkr_abbrev) - Length(g.mrkr_abbrev) + 1 )
                                          FOR ( Length(g.mrkr_abbrev) ))                                          )
and antibody.mrkr_type = 'ATB';