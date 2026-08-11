--liquibase formatted sql

-- runOnChange:true because both tables below are disposable staging scratch that
-- this changeset drops and rebuilds from scratch; environments that applied an
-- earlier variant should pick up edits rather than fail on a checksum mismatch.
--changeset cmpich:0040-ZFIN-10330-moens-hcr-expression-staging runOnChange:true

-- ZFIN-10330 / ZFINHELP-5462 : Moens lab HCR in situ expression data (staging).
--
-- Cleaned, column-complete source data for the HCR in situ expression annotations,
-- one row per (figure, superterm, subterm) observation. Source spreadsheet:
-- Italia_ImageDataLoad_MoensLab-2.xlsx. This changeset ONLY stages the cleaned
-- data; moens-hcr-expression-records.sql, run by org.zfin.figure.MoensHcrImageLoad,
-- resolves the OBO/ZFS ids to term/stage ZDB ids and creates the fish_experiment /
-- expression_experiment2 / expression_figure_stage / expression_result2 records under
-- ZDB-PUB-260717-17.
--
-- Cleaning applied to the raw spreadsheet:
--   * legend rows (col1 = "Color Key:" / "Yellow = ...") dropped
--   * blank ZFS recovered from the free-text Stage column
--       (efna2b 6 dpf -> ZFS:0000038 ; slc10a4/vwc2l 26 hpf -> ZFS:0000029)
--   * multi-structure rows split to ONE superterm + ONE subterm per row
--     (super/sub pipe-delimited values expanded; single-super*single-sub kept)
--   * multi-GENE rows split to ONE gene per row: a cell holding two gene ids is one
--     expression annotation per gene. Affects sheet rows 88 (vipb + isl1a), 19 and 21
--     (both mnx2b + cntn2). Rows 19 and 21 additionally needed per-gene STRUCTURES, not
--     just a per-gene split, and row 21 had no mnx2b gene id at all -- both resolved
--     2026-08-11, see below and MULTI_GENE_ROWS.txt.
--   * mhel_image_stem = source image filename without the .tif extension; joins to
--     moens_hcr_image_load_map (written by org.zfin.figure.MoensHcrImageLoad) to reach
--     the loaded figure. Every row maps to one of the 105 loaded figures.
--   * of the 6 rows whose image filename could not be reconciled to a file on disk
--     (spreadsheet rows 17,19,21,29,57,92), the curator confirmed on 2026-08-04 that
--     17, 57 and 92 should use the disk image as found -- those are now included.
--     On 2026-08-10 the curator supplied images for the remaining three. All six are now
--     included; nothing is held back. The decisions that unblocked rows 19 and 21 --
--       - RESOLVED 2026-08-11: both rows label abducens motor neuron as ZFA:0007133,
--         which is MIGRATORY FACIAL motor neuron. The curator confirmed the abducens
--         annotation on these two rows uses ZFA:0007132 (abducens motor neuron).
--         Scoped to rows 19 and 21 only: the five staged rows carrying ZFA:0007133
--         (sheet rows 40, 90, 103 -- gata2b, vwc2l, znf385d) are correct, their captions
--         all read migrating / have-migrated facial motor neuron at 26-28 hpf, so
--         nothing already loaded is affected.
--       - RESOLVED 2026-08-11: row 19 splits into two genes against two DIFFERENT
--         structures, and the superterms are the rhombomeres the caption names, NOT the
--         plain hindbrain the sheet's anatomy column records. Curator confirmed:
--             mnx2b  ZDB-GENE-040415-2  -> ZFA:0007132 abducens motor neuron,
--                                          super ZFA:0000823 (r5) and ZFA:0000069 (r6)
--             cntn2  ZDB-GENE-990630-12 -> ZFA:0007129 facial motor neuron,
--                                          super ZFA:0000069 (r6)
--         Two superterms for mnx2b = two rows, so sheet row 19 becomes 3 staging rows
--         (same broadcast shape already used for sheet rows 17 and 29).
--       - RESOLVED 2026-08-11: row 21 is a multi-GENE row mis-recorded as a
--         multi-structure row. Its gene column holds only cntn2, so mnx2b's id was added
--         and its two substructures assigned to the two genes. It is NOT row 19 at a later
--         stage: this caption also states cntn2 is expressed in a subset of the abducens
--         neurons, so cntn2 is recorded in abducens as well as facial (5 rows vs row 19's
--         3). See its rows below.
--       - RESOLVED 2026-08-11: the manifest stems for both rows name files that do not
--         exist on disk; four mnx2b files were delivered instead. Row 19 takes the cyan
--         48hpf/emb2/260114 file, and its caption was amended "mnx2b (red)" -> "(cyan)" to
--         match. Row 21 takes the cyan_annotated 3dpf/emb1/251219 file; its caption already
--         said "mnx2b (cyan)" and needed no amendment.
--         Both rows were APPENDED to the manifest rather than flipped in place, following
--         sheet rows 17/57/92/29, so the earlier rows keep the Fig. N they already had.
--         Rows 19 and 21 are Fig. 104 and Fig. 105.
--       - STILL OPEN: mnx2b_red_cntn2_magenta_3dpf_emb2_251219 is emb2 and matches no
--         sheet row at all -- a spare, or a row missing from the spreadsheet?
--   * uniform columns: fish = ZDB-FISH-260717-2, assay = MMO_0000643 (HCR in situ),
--     publication = ZDB-PUB-260717-17, expression_found = true
--
-- Stage is a single time point, so start stage = end stage = mhel_zfs_id.
-- Superterm/subterm are ZFA OBO ids (resolved to term_zdb_id downstream).

drop table if exists moens_hcr_expression_load;

-- Written one row at a time by org.zfin.figure.MoensHcrImageLoad as each image is
-- loaded. Keeps the source filename -> loaded figure/image mapping outside of
-- fig_label / img_label, which stay on the site-wide "Fig. N" convention. Doubles as
-- the loader's re-run guard (a stem present here is skipped) and as the join
-- moens-hcr-expression-records.sql uses to reach fig_zdb_id.
--
-- NOT dropped here, unlike the staging data table above. This changeset is
-- runOnChange:true, and its contents are written by a separate load step, not by this
-- file -- dropping it on every re-run would discard the record of what was already
-- loaded and the loader would then create a second set of 105 figures on its next run.
-- Both staging tables are dropped by moens-hcr-drop-staging.sql, run explicitly via
-- ./gradlew loadMoensHcrImages -PdropStaging once the load has been checked.
create table if not exists moens_hcr_image_load_map (
  mhilm_image_stem      text    primary key,  -- source filename without .tif
  mhilm_fig_zdb_id      text    not null,     -- ZDB-FIG-... created by the loader
  mhilm_img_zdb_id      text    not null,     -- ZDB-IMAGE-... created by the loader
  mhilm_fig_label       text    not null      -- 'Fig. N' assigned by manifest order
);

create table moens_hcr_expression_load (
  mhel_pk_id            serial primary key,
  mhel_image_stem       text    not null,   -- -> moens_hcr_image_load_map.mhilm_image_stem
  mhel_gene_zdb_id      text    not null,   -- ZDB-GENE-...
  mhel_gene_symbol      text    not null,   -- for QC / readability
  mhel_zfs_id           text    not null,   -- ZFS:....  (start = end = this stage)
  mhel_superterm_zfa    text    not null,   -- ZFA OBO id
  mhel_subterm_zfa      text,               -- ZFA OBO id (nullable)
  mhel_fish_zdb_id      text    not null default 'ZDB-FISH-260717-2',
  mhel_assay_mmo        text    not null default 'MMO_0000643',
  mhel_publication      text    not null default 'ZDB-PUB-260717-17',
  mhel_expression_found boolean not null default true
);

insert into moens_hcr_expression_load
  (mhel_image_stem, mhel_gene_zdb_id, mhel_gene_symbol, mhel_zfs_id, mhel_superterm_zfa, mhel_subterm_zfa)
values
  ('btbd11b_magenta_2d_emb2_260130_mIX_ventral_(RGB)_with lines', 'ZDB-GENE-050419-99', 'btbd11b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007127'),
  ('calca_magenta_48h_20250917_emb4_mX_ventral_(RGB)', 'ZDB-GENE-040718-173', 'calca', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('calca_magenta_3dpf_20241105_emb1_ventral_mX_(RGB)', 'ZDB-GENE-040718-173', 'calca', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('calca_magenta_3dpf_20241105_emb5_lateral_vagus_(RGB)', 'ZDB-GENE-040718-173', 'calca', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('calca_magenta_6dpf_emb1_20251031_mX_dorsal_flipped_(RGB)', 'ZDB-GENE-040718-173', 'calca', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('cdh5_48h_20250328__emb1_mIII_mIV_single(RGB)', 'ZDB-GENE-040816-1', 'cdh5', 'ZFS:0000033', 'ZFA:0000128', 'ZFA:0007128'),
  ('cdh5_magenta_3dpf_emb3_20251117_mIII_ventral_(RGB)(1)', 'ZDB-GENE-040816-1', 'cdh5', 'ZFS:0000035', 'ZFA:0000128', 'ZFA:0007128'),
  ('chrna3_magenta_28h_20250115_emb4_ventral_mVII_(RGB)', 'ZDB-GENE-070822-1', 'chrna3', 'ZFS:0000029', 'ZFA:0000949', 'ZFA:0007129'),
  ('chrna3_magenta_48h_emb5_251209_mVII r7_ventral_PMT Light_(RGB)', 'ZDB-GENE-070822-1', 'chrna3', 'ZFS:0000033', 'ZFA:0000069', 'ZFA:0007129'),
  ('chrna3_magenta_48h_emb5_251209_mVII r7_ventral_PMT Light_(RGB)', 'ZDB-GENE-070822-1', 'chrna3', 'ZFS:0000033', 'ZFA:0000949', 'ZFA:0007129'),
  ('chrna3_magenta_3dpf_20250930_emb2_mVII r7_ventral_(RGB)', 'ZDB-GENE-070822-1', 'chrna3', 'ZFS:0000035', 'ZFA:0000069', 'ZFA:0007129'),
  ('chrna3_magenta_3dpf_20250930_emb2_mVII r7_ventral_(RGB)', 'ZDB-GENE-070822-1', 'chrna3', 'ZFS:0000035', 'ZFA:0000949', 'ZFA:0007129'),
  ('chrna3_magenta_6dpf_20250206_emb4_ventral_mVII r6_(RGB)', 'ZDB-GENE-070822-1', 'chrna3', 'ZFS:0000038', 'ZFA:0000069', 'ZFA:0007129'),
  ('MAX_cntn2_28h_20250910_emb3_mV_ventral_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000029', 'ZFA:0000822', 'ZFA:0007130'),
  ('MAX_cntn2_28h_20250910_emb3_mV_ventral_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000029', 'ZFA:0000948', 'ZFA:0007130'),
  ('MAX_20251210_cntn2_26h_isl1Kaede_prim6_cbm_emb3_mVII.tif (RGB)-1', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000029', 'ZFA:0000069', 'ZFA:0007129'),
  ('MAX_20251210_cntn2_26h_isl1Kaede_prim6_cbm_emb3_mVII.tif (RGB)-1', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000029', 'ZFA:0000949', 'ZFA:0007129'),
  ('cntn2_magenta_48h_20250815_emb5_mIV_ventral_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007131'),
  ('cntn2_mangenta_48h_20250815_emb5_mV r3_ventral_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000033', 'ZFA:0000948', 'ZFA:0007130'),
  ('cntn2_magenta_48hpf_emb2_20260114_mX_ventral_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('cntn2_magenta_3dpf_20250108_emb4_ventral_mV r3_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000035', 'ZFA:0000948', 'ZFA:0007130'),
  ('cntn2_magenta_3dpf_20241011_emb1_ventral_mX_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('cntn2_magenta_6dpf_emb3_20251219_mV r3_ventral_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000038', 'ZFA:0000948', 'ZFA:0007130'),
  ('cntn2_magenta_6dpf_emb2_20251219_mVII r6_ventral_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007129'),
  ('dgkaa_magenta_48hpf_20251010_emb4_mX_lateral_(RGB)', 'ZDB-GENE-060616-305', 'dgkaa', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('dgkaa_magenta_3dpf_20240923_emb2_dorsal_mX_(RGB)', 'ZDB-GENE-060616-305', 'dgkaa', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('dgkaa_magenta_3dpf_20251009_emb4_mX_lateral_(RGB)', 'ZDB-GENE-060616-305', 'dgkaa', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('MAX_crabp1b_48h_20250502__emb1_mIX.tif (RGB)-1', 'ZDB-GENE-040624-3', 'crabp1b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007127'),
  ('MAX_crabp1b_48h_20250502__emb1_mIX.tif (RGB)-1', 'ZDB-GENE-040624-3', 'crabp1b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  -- Sheet row 29, the 3 dpf counterpart of the two rows above. Held since the original
  -- load because its spreadsheet filename matched nothing on disk; the curator supplied
  -- the image on 2026-08-10. Same broadcast shape as row 28: one superterm (hindbrain)
  -- against both substructures the sheet lists, glossopharyngeal and vagus motor neuron.
  -- NOTE the supplied file is byte-identical to the previously unmatched disk file
  -- crabp1b_3dpf__20240920_emb3_ventral_mX_single(RGB).tif, renamed mX -> mIX. The sheet,
  -- the caption ("CN IX and/or anterior-most CN X") and row 28 all carry both nuclei, so
  -- both are staged; drop the ZFA:0007126 row if the curator means CN IX alone.
  ('crabp1b_magenta_3dpf_20240920_emb3_ventral_mIX_(RGB)', 'ZDB-GENE-040624-3', 'crabp1b', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007127'),
  ('crabp1b_magenta_3dpf_20240920_emb3_ventral_mIX_(RGB)', 'ZDB-GENE-040624-3', 'crabp1b', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('efna2b_magenta_48h_20250623_emb4_mX_ventral_(RGB)', 'ZDB-GENE-141120-2', 'efna2b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('efna2b_48h_20250623_emb1_mX_lateral_(RGB)', 'ZDB-GENE-141120-2', 'efna2b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('efna2b_magenta_3dpf_20250527_emb4_mX_ventral_(RGB)', 'ZDB-GENE-141120-2', 'efna2b', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('efna2b_magenta_3dpf_20250527_emb5_mX_lateral_(RGB)', 'ZDB-GENE-141120-2', 'efna2b', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('efna2b_magenta_6d_20250618_emb4_mX_ventral_(RGB)', 'ZDB-GENE-141120-2', 'efna2b', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('erfl1_magenta_48h_20250620_emb2_lateral_mX_mIX_(RGB)', 'ZDB-GENE-090529-3', 'erfl1', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007127'),
  ('erfl1_magenta_48h_20250620_emb2_lateral_mX_mIX_(RGB)', 'ZDB-GENE-090529-3', 'erfl1', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('esrrga_magenta_48h_251119_emb2_faint mIII_ventral_(RGB)', 'ZDB-GENE-030821-2', 'esrrga', 'ZFS:0000033', 'ZFA:0000128', 'ZFA:0007128'),
  ('esrrga_magenta_48h_251119_emb3_mIV_ventral_(RGB)', 'ZDB-GENE-030821-2', 'esrrga', 'ZFS:0000033', 'ZFA:0000128', 'ZFA:0007131'),
  ('esrrga_magenta_3dpf_20251006_emb4_faint mIII_ventral_(RGB)', 'ZDB-GENE-030821-2', 'esrrga', 'ZFS:0000035', 'ZFA:0000128', 'ZFA:0007128'),
  ('esrrga_magenta_3dpf_20251006_emb4_mIV_ventral_(RGB)', 'ZDB-GENE-030821-2', 'esrrga', 'ZFS:0000035', 'ZFA:0000128', 'ZFA:0007131'),
  ('gata2b_magenta_26h_20241119_emb1_ventral_mVII r6_(RGB)', 'ZDB-GENE-040718-440', 'gata2b', 'ZFS:0000029', 'ZFA:0000029', 'ZFA:0007133'),
  ('gata2b_magenta_48hpf_20251029_emb1_mVII r6_PMT light_(RGB)', 'ZDB-GENE-040718-440', 'gata2b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007129'),
  ('gcgra_magenta_48h_20250423_emb3_lateral_mX_(RGB)', 'ZDB-GENE-050516-1', 'gcgra', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('gulp1a_magenta_48h_20250311_emb2_lateral_mX_(RGB)', 'ZDB-GENE-030616-21', 'gulp1a', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('hoxc6b_48h_20250908_emb4_mX_lateral_single(RGB)', 'ZDB-GENE-000822-1', 'hoxc6b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('hoxc6b_magenta_3dpf_20241209_lateral_vagus_emb3_side1-1_(RGB)', 'ZDB-GENE-000822-1', 'hoxc6b', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('kcng1_magenta_48hpf_20251030_emb4_mX_ventral_(RGB)', 'ZDB-GENE-080220-5', 'kcng1', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('kcng1_magenta_3dpf_20241004_emb3_lateral_mX_(RGB)', 'ZDB-GENE-080220-5', 'kcng1', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('otx2a_48hpf_emb6_20251117_mIV_mIII_single.tif (RGB)-1', 'ZDB-GENE-980526-27', 'otx2a', 'ZFS:0000033', 'ZFA:0000128', 'ZFA:0007128'),
  ('otx2a_magenta_3dpf_emb3_20251117_mIII_ventral_(RGB)', 'ZDB-GENE-980526-27', 'otx2a', 'ZFS:0000035', 'ZFA:0000128', 'ZFA:0007128'),
  ('otx2a_magenta_6dpf_20250131_emb2_ventral_mIII_(RGB)', 'ZDB-GENE-980526-27', 'otx2a', 'ZFS:0000038', 'ZFA:0000128', 'ZFA:0007128'),
  ('pax5_magenta_48h_20250507_emb3_mIII_dorsal_(RGB)', 'ZDB-GENE-001030-2', 'pax5', 'ZFS:0000033', 'ZFA:0000128', 'ZFA:0007128'),
  ('pcdh15b_magenta_28h_emb5_260420_mV r2_ventral_(RGB)', 'ZDB-GENE-050214-1', 'pcdh15b', 'ZFS:0000029', 'ZFA:0000822', 'ZFA:0007130'),
  ('pcdh15b_magenta_48h_emb1_260418_mV r2_ventral_PMT Light_(RGB)', 'ZDB-GENE-050214-1', 'pcdh15b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007130'),
  ('plcb3_magenta_48h_20250312_emb3_lateral_mX_(RGB)', 'ZDB-GENE-030616-594', 'plcb3', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('plcb3_magenta_3dpf_20241018_emb1_ventral_mX_(RGB)', 'ZDB-GENE-030616-594', 'plcb3', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('plcb3_magenta_3dpf_20241018_emb6_lateral_mX_(RGB)', 'ZDB-GENE-030616-594', 'plcb3', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('ptgir_magenta_3dpf_20241105_emb4_ventral_mV r2_(RGB)', 'ZDB-GENE-120511-1', 'ptgir', 'ZFS:0000035', 'ZFA:0000822', 'ZFA:0007130'),
  ('ptgir_magenta_6dpf_20241205_emb4_dorsal_mV r2_(RGB)', 'ZDB-GENE-120511-1', 'ptgir', 'ZFS:0000038', 'ZFA:0000822', 'ZFA:0007130'),
  ('qrfpr_magenta_26h_20241119_emb1_mV_(RGB)', 'ZDB-GENE-091204-354', 'qrfpr4', 'ZFS:0000029', 'ZFA:0000822', 'ZFA:0007130'),
  ('qrfpr4_magenta_48hpf_20251007_emb3_mV r2_ventral_(RGB)', 'ZDB-GENE-091204-354', 'qrfpr4', 'ZFS:0000033', 'ZFA:0000822', 'ZFA:0007130'),
  ('MAX_qrfpr4_magenta_6dpf_20241126_emb1_ventral_mV r2_(RGB)', 'ZDB-GENE-091204-354', 'qrfpr4', 'ZFS:0000038', 'ZFA:0000822', 'ZFA:0007130'),
  ('qrfpr4_magenta_3dpf_20250925_emb1_mV r2_ventral_(RGB)', 'ZDB-GENE-091204-354', 'qrfpr4', 'ZFS:0000035', 'ZFA:0000822', 'ZFA:0007130'),
  ('sall3a_magenta_28h_20250117_emb3_ventral_faint mX_(RGB)', 'ZDB-GENE-020228-4', 'sall3a', 'ZFS:0000029', 'ZFA:0000029', 'ZFA:0007126'),
  ('sall3a_48h_emb5_260424_mX_lateral_single(RGB)', 'ZDB-GENE-020228-4', 'sall3a', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('MAX_sall3a_magenta_48h_emb4_260424_mX_ventral_(RGB)', 'ZDB-GENE-020228-4', 'sall3a', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('sall3a_magenta_3dpf_emb6=emb1_20240823_dorsal_mX_(RGB)', 'ZDB-GENE-020228-4', 'sall3a', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('shox2_48h_emb3_260424_mV_single(RGB)', 'ZDB-GENE-040426-1457', 'shox2', 'ZFS:0000033', 'ZFA:0000948', 'ZFA:0007130'),
  ('shox2_3dpf_emb3_260427_mV_single(RGB)', 'ZDB-GENE-040426-1457', 'shox2', 'ZFS:0000035', 'ZFA:0000948', 'ZFA:0007130'),
  ('sim1a_magenta_48h_emb3_251219_mIIId_ventral_PMT Light_(RGB)', 'ZDB-GENE-020829-1', 'sim1a', 'ZFS:0000033', 'ZFA:0000128', 'ZFA:0007128'),
  ('slc10a4_magenta_26h_20241015_emb2_ventral_mX_(RGB)', 'ZDB-GENE-041014-249', 'slc10a4', 'ZFS:0000029', 'ZFA:0000029', 'ZFA:0007126'),
  ('slc10a4_magenta_48h_20250912_emb4_mX_ventral_(RGB)', 'ZDB-GENE-041014-249', 'slc10a4', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('slc10a4_magenta_48h_20250912_emb5_mX_lateral_(RGB)', 'ZDB-GENE-041014-249', 'slc10a4', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('slc10a4_magenta_3dpf_20241002_emb4_lateral_mX_(RGB)', 'ZDB-GENE-041014-249', 'slc10a4', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('slc10a4_magenta_6dpf_20250206_emb4_ventral_mX_(RGB)', 'ZDB-GENE-041014-249', 'slc10a4', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('tbx5b_magenta_48h_20250331_emb3_ventral_mX_(RGB)', 'ZDB-GENE-060601-2', 'tbx5b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('tbx5b_magenta_48h_20250331_emb1_lateral_mX_(RGB)', 'ZDB-GENE-060601-2', 'tbx5b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('tbx5b_magenta_3dpf_20241217_emb1_ventral_mX_(RGB)', 'ZDB-GENE-060601-2', 'tbx5b', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('tbx5b_magenta_6dpf_20250214_emb1_lateral_mX_(RGB)', 'ZDB-GENE-060601-2', 'tbx5b', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('tbx5b_magenta_6dpf_20250509_emb1_mX_dorsal_(RGB)', 'ZDB-GENE-060601-2', 'tbx5b', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('tmeff2b_magenta_48h_20250821_emb3_mX_ventral_(RGB)', 'ZDB-GENE-101001-4', 'tmeff2b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('tmeff2b_magenta_48h_20250905_emb3_mX_lateral_(RGB)', 'ZDB-GENE-101001-4', 'tmeff2b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('tmeff2b_magenta_3d_20240709_lateral_mX_(RGB)', 'ZDB-GENE-101001-4', 'tmeff2b', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('tmeff2b_magenta_6dpf_20250929_emb4_mX_ventral_(RGB)', 'ZDB-GENE-101001-4', 'tmeff2b', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('tmeff2b_magenta_6dpf_20250929_emb1_mX_lateral_(RGB)', 'ZDB-GENE-101001-4', 'tmeff2b', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('trh_magenta_28hpf_20251024_emb3_mV r2_ventral_(RGB)', 'ZDB-GENE-020930-1', 'trh', 'ZFS:0000029', 'ZFA:0000822', 'ZFA:0007130'),
  -- Sheet row 87's symbol cell reads "done - vipb"; the "done - " is a curator working note
  -- left in the sheet, not a second gene. Single gene id, recorded here under its symbol.
  ('vipb_magenta_3d_20240924_emb4_dorsal_mX_2_(RGB)', 'ZDB-GENE-080225-22', 'vipb', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  -- Sheet row 88 carried two gene ids in one cell ('vipb and isl1a', a dual-channel HCR
  -- image). Per curator decision, a multi-gene cell becomes one expression annotation per
  -- gene, so this is split into the two rows below sharing the row's stage and structure.
  ('vipb_magenta_isl1a_cyan_3dpf_emb5_20260113_mX_ventral_(RGB)', 'ZDB-GENE-080225-22', 'vipb', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('vipb_magenta_isl1a_cyan_3dpf_emb5_20260113_mX_ventral_(RGB)', 'ZDB-GENE-980526-112', 'isl1a', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('vipb_magenta_6dpf_20241206_emb3_dorsal_mX_(RGB)', 'ZDB-GENE-080225-22', 'vipb', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('vwc2l_magenta_26hpf_20241018_emb4_ventral_mVII_(RGB)', 'ZDB-GENE-081104-169', 'vwc2l', 'ZFS:0000029', 'ZFA:0000069', 'ZFA:0007133'),
  ('vwc2l_magenta_26hpf_20241018_emb4_ventral_mVII_(RGB)', 'ZDB-GENE-081104-169', 'vwc2l', 'ZFS:0000029', 'ZFA:0000949', 'ZFA:0007133'),
  ('MAX_vwc2l_48h_emb1_260505_mX_ventral_(RGB)', 'ZDB-GENE-081104-169', 'vwc2l', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007127'),
  ('MAX_vwc2l_48h_emb1_260505_mX_ventral_(RGB)', 'ZDB-GENE-081104-169', 'vwc2l', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('zeb2a_magenta_48hpf_20251006_emb3_mX_ventral_(RGB)', 'ZDB-GENE-070912-553', 'zeb2a', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('zeb2a_48hpf_20251006_emb4_mX_lateral_single_(RGB)', 'ZDB-GENE-070912-553', 'zeb2a', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('zeb2a_magenta_3dpf_20250325_emb3_ventral_mX_(RGB)', 'ZDB-GENE-070912-553', 'zeb2a', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('zeb2a_magenta_3dpf_20250326_emb3_lateral_mX_(RGB)', 'ZDB-GENE-070912-553', 'zeb2a', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('zeb2a_magenta_6dpf_20250326_emb2_lateral_flipped_mX_(RGB)', 'ZDB-GENE-070912-553', 'zeb2a', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('zeb2b_magenta_48h_20250825_emb5_mX_lateral_(RGB)', 'ZDB-GENE-080717-1', 'zeb2b', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),
  ('zeb2b_magenta_3dpf_20250502_emb2_mX_ventral_(RGB)', 'ZDB-GENE-080717-1', 'zeb2b', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('zeb2b_magenta_3dpf_20250422_emb3_lateral_twisted_mX_.tif (RGB)', 'ZDB-GENE-080717-1', 'zeb2b', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('zeb2b_magenta_6dpf_20250515_emb2_vagus_dorsal_(RGB)', 'ZDB-GENE-080717-1', 'zeb2b', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('zeb2b_magenta_6dpf_20251016_emb1_mX_lateral_(RGB)', 'ZDB-GENE-080717-1', 'zeb2b', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007126'),
  ('MAX_znf385d_magenta_28h_emb4_251209_mVII_best(RGB)', 'ZDB-GENE-130201-2', 'znf385d', 'ZFS:0000029', 'ZFA:0000069', 'ZFA:0007133'),
  ('MAX_znf385d_magenta_28h_emb4_251209_mVII_best(RGB)', 'ZDB-GENE-130201-2', 'znf385d', 'ZFS:0000029', 'ZFA:0000949', 'ZFA:0007133'),
  ('znf385d_magenta_48h_emb1_251209_mVII r6_ventral_(RGB)', 'ZDB-GENE-130201-2', 'znf385d', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007129'),
  ('znf385d_magenta_3dpf_emb3_251209_mVII r7_ventral_(RGB)', 'ZDB-GENE-130201-2', 'znf385d', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007129'),
  ('znf385d_magenta_6dpf_20250204_emb4_ventral_mVII r7_(RGB)', 'ZDB-GENE-130201-2', 'znf385d', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007129'),

  -- Sheet rows 17, 57 and 92: originally held back because the image filename in the
  -- spreadsheet did not match any file on disk. Curator confirmed 2026-08-04 to use the
  -- disk image as found, so these are now loaded and annotated here from their
  -- spreadsheet rows unchanged. Sheet rows 19 and 29 were resolved later and follow
  -- below; sheet row 21 is still excluded (see the header).
  -- sheet row 17 -- cntn2, 48 hpf, r6 + r7 / facial motor neuron (2 superterms -> 2 rows)
  ('cntn2_magenta_48h_20250815_emb4_mVII r7_ventral_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000033', 'ZFA:0000069', 'ZFA:0007129'),
  ('cntn2_magenta_48h_20250815_emb4_mVII r7_ventral_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000033', 'ZFA:0000949', 'ZFA:0007129'),
  -- sheet row 57 -- ptgir, 48 hpf, rhombomere 2 / trigeminal motor neuron
  ('ptgir_magenta_48hpf_20250930_emb2_mV r2_ventral_PMT Light_(RGB)', 'ZDB-GENE-120511-1', 'ptgir', 'ZFS:0000033', 'ZFA:0000822', 'ZFA:0007130'),
  -- sheet row 92 -- vwc2l, 48 hpf (2 dpf), hindbrain / vagus motor neuron
  ('vwc2l_magenta_2dpf_20260529_emb1_lateral_mX_(RGB)', 'ZDB-GENE-081104-169', 'vwc2l', 'ZFS:0000033', 'ZFA:0000029', 'ZFA:0007126'),

  -- Sheet row 19 -- the multi-gene, multi-structure row, resolved by the curator on
  -- 2026-08-11 (see the header for the three decisions). Two genes against two DIFFERENT
  -- structures, and the superterms are the rhombomeres the caption names rather than the
  -- plain hindbrain the sheet's anatomy column recorded:
  --     mnx2b -> abducens motor neuron (ZFA:0007132) in r5 and r6
  --     cntn2 -> facial motor neuron   (ZFA:0007129) in r6
  -- ZFA:0007132 is used, NOT the sheet's ZFA:0007133 (migratory facial motor neuron).
  -- The image is the cyan file supplied 2026-08-10, appended to the manifest rather than
  -- flipped in place so the earlier figure numbering stays put, and its caption was
  -- amended red -> cyan to match the image.
  ('mnx2b_cyan_cntn2_magenta_48hpf_emb2_260114_mVII_mVI_ventral_annotated_(RGB)', 'ZDB-GENE-040415-2', 'mnx2b', 'ZFS:0000033', 'ZFA:0000823', 'ZFA:0007132'),
  ('mnx2b_cyan_cntn2_magenta_48hpf_emb2_260114_mVII_mVI_ventral_annotated_(RGB)', 'ZDB-GENE-040415-2', 'mnx2b', 'ZFS:0000033', 'ZFA:0000069', 'ZFA:0007132'),
  ('mnx2b_cyan_cntn2_magenta_48hpf_emb2_260114_mVII_mVI_ventral_annotated_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000033', 'ZFA:0000069', 'ZFA:0007129'),

  -- Sheet row 21 -- the 3 dpf counterpart of row 19, resolved by the curator on
  -- 2026-08-11. Recorded as a multi-GENE row, not the multi-structure row the sheet made
  -- it: the sheet's gene column holds only cntn2, so mnx2b's id is added here, and its
  -- two substructures (facial | abducens) belong to the two genes.
  --
  -- NOT simply row 19 at a later stage. Row 19's caption puts cntn2 in facial neurons
  -- only; this caption additionally says "Mnx2 marks CN VI motor neurons, some of which
  -- also express cntn2 at this stage", so cntn2 is recorded in abducens as well as in
  -- facial -- 5 rows rather than row 19's 3:
  --     mnx2b -> abducens (ZFA:0007132) in r5 and r6
  --     cntn2 -> abducens (ZFA:0007132) in r5 and r6   <- the "also express" statement
  --     cntn2 -> facial   (ZFA:0007129) in r6
  -- ZFA:0007132 again replaces the sheet's ZFA:0007133 (migratory facial motor neuron).
  ('mnx2b_cyan_cntn2_magenta_3dpf_emb1_251219_mVII_mVI_ventral_annotated_(RGB)', 'ZDB-GENE-040415-2', 'mnx2b', 'ZFS:0000035', 'ZFA:0000823', 'ZFA:0007132'),
  ('mnx2b_cyan_cntn2_magenta_3dpf_emb1_251219_mVII_mVI_ventral_annotated_(RGB)', 'ZDB-GENE-040415-2', 'mnx2b', 'ZFS:0000035', 'ZFA:0000069', 'ZFA:0007132'),
  ('mnx2b_cyan_cntn2_magenta_3dpf_emb1_251219_mVII_mVI_ventral_annotated_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000035', 'ZFA:0000823', 'ZFA:0007132'),
  ('mnx2b_cyan_cntn2_magenta_3dpf_emb1_251219_mVII_mVI_ventral_annotated_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000035', 'ZFA:0000069', 'ZFA:0007132'),
  ('mnx2b_cyan_cntn2_magenta_3dpf_emb1_251219_mVII_mVI_ventral_annotated_(RGB)', 'ZDB-GENE-990630-12', 'cntn2', 'ZFS:0000035', 'ZFA:0000069', 'ZFA:0007129');
