--liquibase formatted sql

-- ZFIN-10461: the NCBI RefSeq GRCz12tu annotation (GCF_049306965.1) adds a
-- second GRCz12tu genome location -- a different accession and coordinates
-- than the one these 16 genes already had on file -- alongside their existing
-- NCBILoader/GRCz12tu row. This changeset applies NCBIGff3Processor's own two
-- direct writes for that: a new coordinate row per gene here, and the gene's
-- annotation status flipping to Current below.
--
-- It does NOT populate gff3_ncbi / gff3_ncbi_attribute (Load-NCBI-GFF3-File's
-- staging tables for the full parsed GFF3 -- tens of millions of rows, not
-- something a migration should carry). Ordinarily that is fine: nothing reads
-- them until the next Load-NCBI-GFF3-File run truncates and refills them
-- (Gff3NcbiDAO.truncateStagingTables). But NCBI-Gene-Load-Java's
-- markerAssemblyUpdate.sql is the one exception -- it runs on its own separate
-- schedule and reads whatever Load-NCBI-GFF3-File last left in those tables to
-- backfill marker_assembly / sequence_feature_chromosome_location_generated
-- for other genes, well beyond the 16 here. An environment that needs that job
-- to behave correctly still needs an actual Load-NCBI-GFF3-File run (or the
-- staging tables populated some other way) -- this changeset alone is not a
-- substitute for that.

--changeset rtaylor:ZFIN-10461-ncbi-gff3-new-genome-locations
INSERT INTO sequence_feature_chromosome_location_generated
    (sfclg_chromosome, sfclg_data_zdb_id, sfclg_acc_num, sfclg_start, sfclg_end, sfclg_location_source, sfclg_assembly)
VALUES
    ('7', 'ZDB-GENE-001106-5', '137487204', 17542766, 17545495, 'NCBILoader', 'GRCz12tu'),
    ('4', 'ZDB-GENE-030131-2002', '141381820', 55138308, 55146459, 'NCBILoader', 'GRCz12tu'),
    ('22', 'ZDB-GENE-040718-452', '562636', 25908834, 25921057, 'NCBILoader', 'GRCz12tu'),
    ('14', 'ZDB-GENE-041118-14', '137487556', 2789520, 2793747, 'NCBILoader', 'GRCz12tu'),
    ('3', 'ZDB-GENE-050306-35', '101884644', 5888457, 5922712, 'NCBILoader', 'GRCz12tu'),
    ('3', 'ZDB-GENE-061106-4', '101883788', 3548941, 3563612, 'NCBILoader', 'GRCz12tu'),
    ('1', 'ZDB-GENE-070720-19', '141385605', 26008244, 26011301, 'NCBILoader', 'GRCz12tu'),
    ('1', 'ZDB-GENE-071004-9', '100334800', 62234180, 62235671, 'NCBILoader', 'GRCz12tu'),
    ('4', 'ZDB-GENE-071004-94', '100537853', 57736007, 57747250, 'NCBILoader', 'GRCz12tu'),
    ('8', 'ZDB-GENE-080225-10', '137496355', 26443407, 26470994, 'NCBILoader', 'GRCz12tu'),
    ('21', 'ZDB-GENE-081031-87', '110437819', 2991706, 2993926, 'NCBILoader', 'GRCz12tu'),
    ('25', 'ZDB-GENE-081104-135', '402845', 4175208, 4251494, 'NCBILoader', 'GRCz12tu'),
    ('8', 'ZDB-GENE-081104-348', '101887017', 30235766, 30243443, 'NCBILoader', 'GRCz12tu'),
    ('3', 'ZDB-GENE-131127-97', '101884073', 12834427, 12902071, 'NCBILoader', 'GRCz12tu'),
    ('14', 'ZDB-GENE-141215-46', '141377552', 37931552, 37938823, 'NCBILoader', 'GRCz12tu'),
    ('1', 'ZDB-GENE-260310-7', '101885706', 60972433, 60978721, 'NCBILoader', 'GRCz12tu')
ON CONFLICT ON CONSTRAINT uq_sfclg_unique_location DO NOTHING;

-- Same 16 genes, each already had a marker_annotation_status row (one per
-- gene, unique_marker_annotation_status). NCBIGff3Processor keys its existing-
-- location lookup by NCBI Gene ID (upsertSequenceFeatureChromosomeRecords's
-- geneIDMap, from the NCBI_LOADER genome locations already on file): the
-- accession above is a Gene ID none of these genes had a location under
-- before, so the processor treats it as a new gene->location mapping and
-- unconditionally marks the gene Current -- whether or not it already had a
-- location (under a different, now-superseded Gene ID) is not part of that
-- check. All 16 were "Not in current annotation release" beforehand.
--changeset rtaylor:ZFIN-10461-ncbi-gff3-mark-genes-current
UPDATE marker_annotation_status
SET mas_vt_pk_id = (SELECT vt.vt_id
                     FROM vocabulary_term vt
                     JOIN vocabulary v ON v.v_id = vt.vt_v_id
                     WHERE v.v_name = 'annotation status' AND vt.vt_name = 'Current')
WHERE mas_mrkr_zdb_id IN ('ZDB-GENE-001106-5', 'ZDB-GENE-030131-2002', 'ZDB-GENE-040718-452',
                          'ZDB-GENE-041118-14', 'ZDB-GENE-050306-35', 'ZDB-GENE-061106-4',
                          'ZDB-GENE-070720-19', 'ZDB-GENE-071004-9', 'ZDB-GENE-071004-94',
                          'ZDB-GENE-080225-10', 'ZDB-GENE-081031-87', 'ZDB-GENE-081104-135',
                          'ZDB-GENE-081104-348', 'ZDB-GENE-131127-97', 'ZDB-GENE-141215-46',
                          'ZDB-GENE-260310-7');
