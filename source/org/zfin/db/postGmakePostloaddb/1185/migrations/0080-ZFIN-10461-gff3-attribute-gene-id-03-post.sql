--liquibase formatted sql

-- ZFIN-10461: gff3_ncbi_attribute's gene_id rows (gna_key = 'gene_id') hold a ZDB ID,
-- resolved from ZFIN's own db_link/marker cross-reference at the moment
-- NCBIGff3Processor.addGeneIDToAttributes() runs, not from anything in the downloaded GFF3
-- file. Unlike sequence_feature_chromosome_location_generated, which NcbiGenomeLocationReconciler
-- actively re-aligns as db_link drifts, nothing keeps gff3_ncbi_attribute's gene_id rows current
-- between Load-NCBI-GFF3-File runs: that job truncates and fully rebuilds the table from the
-- GFF3 file on its own rare, manual schedule, and the NCBI-Gene-Load-Java job's own gene_id
-- insert (markerAssemblyUpdate.sql) only ever adds a row, never corrects one that has drifted.
--
-- This applies the same drift correction NcbiGenomeLocationReconciler would apply for
-- sequence_feature_chromosome_location_generated, but for gff3_ncbi_attribute's gene_id rows,
-- staged by 01-pre.sql/02-loaddata.xml from the current db_link/marker state. Each row is
-- matched to a feature by its GFF3 ID plus start/end (gff_pk_id is a surrogate that regenerates
-- on every load and carries no meaning across environments; ID+start+end is unique -- see
-- 0070's sibling reconciliation for the same table's identity problem). This does not touch
-- gff3_ncbi or gff3_ncbi_attribute's other ~15.7M rows, which are an unmodified function of the
-- downloaded GFF3 file and need no correction.
--
-- Three categories, sized as found: 43 genes whose gene_id now resolves to a different ZDB gene
-- than before (a merge or a corrected NCBI Gene ID association), 1903 whose gene_id no longer
-- resolves to any ZFIN gene (the db_link cross-reference it depended on is gone), and 453 that
-- gained a gene_id they did not have before.
--
-- No idempotency guard on the insert: Liquibase's own changeset tracking is what keeps this
-- from double-applying, the same guarantee every other non-idempotent changeset in this repo
-- relies on.

--changeset rtaylor:ZFIN-10461-gff3-attribute-gene-id-changed
UPDATE gff3_ncbi_attribute a
SET gna_value = s.new_zdb
FROM gff3_ncbi g, stg_gff3_gene_id_changed s
WHERE g.gff_start = s.gstart AND g.gff_end = s.gend
  AND substring(g.gff_attributes from 'ID=([^;]+)') = s.gid
  AND a.gna_gff_pk_id = g.gff_pk_id
  AND a.gna_key = 'gene_id'
  AND a.gna_value = s.old_zdb;

--changeset rtaylor:ZFIN-10461-gff3-attribute-gene-id-removed
DELETE FROM gff3_ncbi_attribute a
USING gff3_ncbi g, stg_gff3_gene_id_removed s
WHERE g.gff_start = s.gstart AND g.gff_end = s.gend
  AND substring(g.gff_attributes from 'ID=([^;]+)') = s.gid
  AND a.gna_gff_pk_id = g.gff_pk_id
  AND a.gna_key = 'gene_id'
  AND a.gna_value = s.old_zdb;

--changeset rtaylor:ZFIN-10461-gff3-attribute-gene-id-added
INSERT INTO gff3_ncbi_attribute (gna_pk_id, gna_gff_pk_id, gna_key, gna_value)
SELECT nextval('gff3_ncbi_attribute_seq'), g.gff_pk_id, 'gene_id', s.new_zdb
FROM gff3_ncbi g, stg_gff3_gene_id_added s
WHERE g.gff_start = s.gstart AND g.gff_end = s.gend
  AND substring(g.gff_attributes from 'ID=([^;]+)') = s.gid;

-- A handful of features already carried two gene_id rows before this migration (a pre-existing
-- data-quality issue, not something the three changesets above introduce): the "changed" update
-- above only ever touches the row matching old_zdb, so where the other of the pair already held
-- what is now the correct value, both rows end up identical. NCBIGff3Processor's own fresh
-- gene_id resolution never produces more than one gene_id row per feature, so bring this table
-- into line with that: keep one row per feature, preferring the lowest gna_pk_id.
--changeset rtaylor:ZFIN-10461-gff3-attribute-gene-id-dedup
DELETE FROM gff3_ncbi_attribute
WHERE gna_pk_id IN (
    SELECT gna_pk_id FROM (
        SELECT gna_pk_id, row_number() OVER (PARTITION BY gna_gff_pk_id ORDER BY gna_pk_id) AS rn
        FROM gff3_ncbi_attribute WHERE gna_key = 'gene_id'
    ) ranked WHERE rn > 1
);

--changeset rtaylor:ZFIN-10461-gff3-attribute-gene-id-cleanup
DROP TABLE IF EXISTS stg_gff3_gene_id_changed, stg_gff3_gene_id_removed, stg_gff3_gene_id_added;
