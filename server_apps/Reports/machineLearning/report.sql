BEGIN WORK;

CREATE TEMP TABLE tmp_report (
  zdb_id         varchar(50),
  symbol         varchar(255),
  name           varchar(255),
  pubs_with_go   int DEFAULT 0,
  pubs_with_xpat int DEFAULT 0,
  number_go      int DEFAULT 0,
  number_xpat    int DEFAULT 0
);

INSERT INTO tmp_report (zdb_id, symbol, name)
  SELECT
    mrkr_Zdb_id,
    mrkr_abbrev,
    mrkr_name
  FROM marker
  WHERE mrkr_type = 'GENE';

CREATE UNIQUE INDEX mrkr_indx
  ON tmp_report (zdb_id);

UPDATE tmp_report
SET pubs_with_go = (SELECT count(DISTINCT mrkrgoev_source_zdb_id)
                    FROM marker_go_term_evidence, publication
                    WHERE jtype = 'Journal'
                          AND mrkrgoev_source_zdb_id = publication.zdb_id
                          AND mrkrgoev_mrkr_zdb_id = tmp_report.zdb_id
                          AND pub_completion_date IS NOT NULL)
WHERE exists(SELECT 'x'
             FROM marker_go_term_evidence
             WHERE mrkrgoev_mrkr_zdb_id = zdb_id);

UPDATE tmp_report
SET number_go = (SELECT count(*)
                 FROM marker_go_term_evidence
                 WHERE mrkrgoev_mrkr_zdb_id = zdb_id)
WHERE exists(SELECT 'x'
             FROM marker_go_term_evidence
             WHERE mrkrgoev_mrkr_zdb_id = zdb_id);

SELECT count(*)
FROM tmp_report
WHERE number_go != 0
      AND pubs_with_go = 0;

SELECT *
FROM tmp_report
WHERE number_go != 0
LIMIT 1;

UPDATE tmp_report
SET pubs_with_xpat = (SELECT count(DISTINCT xpatex_source_Zdb_id)
                      FROM expression_experiment2, publication
                      WHERE jtype = 'Journal'
                            AND publication.zdb_id = xpatex_source_zdb_id
                            AND xpatex_gene_zdb_id = tmp_report.zdb_id
                            AND pub_completion_date IS NOT NULL)
WHERE exists(SELECT 'x'
             FROM expression_experiment2
             WHERE xpatex_gene_zdb_id = zdb_id);

UPDATE tmp_report
SET number_xpat = (SELECT count(*)
                   FROM expression_experiment2
                   WHERE xpatex_gene_zdb_id = zdb_id)
WHERE exists(SELECT 'x'
             FROM expression_experiment2
             WHERE xpatex_gene_zdb_id = zdb_id);


SELECT count(*)
FROM tmp_report
WHERE number_xpat != 0
      AND pubs_with_xpat = 0;

SELECT *
FROM tmp_report
WHERE number_xpat != 0
LIMIT 1;


\COPY tmp_report TO machineLearningReport_161129.txt;

--commit work;

ROLLBACK WORK;
