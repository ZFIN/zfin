#!/bin/bash

(${PGBINDIR}/psql ${DBNAME} -q << END

CREATE TEMP TABLE tmp_counts (
    tmp_label     text,
    tmp_count     text
);

INSERT INTO tmp_counts
    VALUES ('type', 'count');

INSERT INTO tmp_counts
    SELECT 'uninf_all', COUNT(mrkr_zdb_id)
    FROM marker
    WHERE SUBSTRING(mrkr_zdb_id from 1 for 8) = 'ZDB-GENE'
    AND mrkr_abbrev LIKE '%:%';

INSERT INTO tmp_counts
    SELECT 'uninf_si', COUNT(mrkr_zdb_id)
    FROM marker
    WHERE SUBSTRING(mrkr_zdb_id from 1 for 8) = 'ZDB-GENE'
    AND mrkr_abbrev LIKE 'si:%';

INSERT INTO tmp_counts
    SELECT 'uninf_zgc', COUNT(mrkr_zdb_id)
    FROM marker
    WHERE SUBSTRING(mrkr_zdb_id from 1 for 8) = 'ZDB-GENE'
    AND mrkr_abbrev LIKE 'zgc:%';

INSERT INTO tmp_counts
    SELECT 'uninf_all_orth', COUNT(mrkr_zdb_id)
    FROM marker
    JOIN ortholog ON mrkr_zdb_id = ortho_zebrafish_gene_zdb_id
    WHERE SUBSTRING(mrkr_zdb_id from 1 for 8) = 'ZDB-GENE'
    AND mrkr_abbrev LIKE '%:%';

INSERT INTO tmp_counts
    SELECT 'uninf_si_orth', COUNT(mrkr_zdb_id)
    FROM marker
    JOIN ortholog ON mrkr_zdb_id = ortho_zebrafish_gene_zdb_id
    WHERE SUBSTRING(mrkr_zdb_id from 1 for 8) = 'ZDB-GENE'
    AND mrkr_abbrev LIKE 'si:%';

INSERT INTO tmp_counts
    SELECT 'uninf_zgc_orth', COUNT(mrkr_zdb_id)
    FROM marker
    JOIN ortholog ON mrkr_zdb_id = ortho_zebrafish_gene_zdb_id
    WHERE SUBSTRING(mrkr_zdb_id from 1 for 8) = 'ZDB-GENE'
    AND mrkr_abbrev LIKE 'zgc:%';

COPY tmp_counts TO STDOUT

END

) > result.log
