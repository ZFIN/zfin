--liquibase formatted sql

-- ZFIN-10367: simplify the per-mutation General section. The separate
-- "ZFIN Record Established" question is now carried by m_allele_in_zfin
-- (relabeled in the form to "ZFIN Record Established"), and the
-- "Cell Genomic Feature" field was redundant leftover from earlier
-- iterations. Drop both columns. No backfill — these only held
-- smoke-test data.

--changeset zirc:zirc-drop-mutation-zfin-record-cell-feature
ALTER TABLE zirc.mutation DROP COLUMN m_zfin_record_established;
ALTER TABLE zirc.mutation DROP COLUMN m_cell_genomic_feature;
