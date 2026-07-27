--liquibase formatted sql
--changeset cmpich:0040-ZFIN-10330-moens-hcr-expression-staging

-- ZFIN-10330 / ZFINHELP-5462 : Moens lab HCR in situ expression data (staging).
--
-- Cleaned, column-complete source data for the HCR in situ expression annotations,
-- one row per (figure, superterm, subterm) observation. Source spreadsheet:
-- Italia_ImageDataLoad_MoensLab-2.xlsx. This changeset ONLY stages the cleaned
-- data; a follow-up changeset resolves the OBO/ZFS ids to term/stage ZDB ids,
-- creates the fish_experiment / expression_experiment2 / expression_figure_stage /
-- expression_result2 records under ZDB-PUB-260717-17, and drops this table.
--
-- Cleaning applied to the raw spreadsheet:
--   * legend rows (col1 = "Color Key:" / "Yellow = ...") dropped
--   * blank ZFS recovered from the free-text Stage column
--       (efna2b 6 dpf -> ZFS:0000038 ; slc10a4/vwc2l 26 hpf -> ZFS:0000029)
--   * multi-structure rows split to ONE superterm + ONE subterm per row
--     (super/sub pipe-delimited values expanded; single-super*single-sub kept)
--   * mhel_fig_label = the loaded figure's label (image filename stem) created by
--     org.zfin.figure.MoensHcrImageLoad; every row maps to one of the 99 loaded figures
--   * 6 rows whose image filename could not be reconciled to a loaded figure
--     (spreadsheet rows 17,19,21,29,57,92) are EXCLUDED pending curator confirmation
--   * uniform columns: fish = ZDB-FISH-260717-2, assay = MMO_0000643 (HCR in situ),
--     publication = ZDB-PUB-260717-17, expression_found = true
--
-- Stage is a single time point, so start stage = end stage = mhel_zfs_id.
-- Superterm/subterm are ZFA OBO ids (resolved to term_zdb_id downstream).

drop table if exists moens_hcr_expression_load;

create table moens_hcr_expression_load (
  mhel_pk_id            serial primary key,
  mhel_fig_label        text    not null,   -- figure label = loaded image filename stem
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
  (mhel_fig_label, mhel_gene_zdb_id, mhel_gene_symbol, mhel_zfs_id, mhel_superterm_zfa, mhel_subterm_zfa)
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
  ('vipb_magenta_3d_20240924_emb4_dorsal_mX_2_(RGB)', 'ZDB-GENE-080225-22', 'done - vipb', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
  ('vipb_magenta_isl1a_cyan_3dpf_emb5_20260113_mX_ventral_(RGB)', 'ZDB-GENE-080225-22, ZDB-GENE-980526-112', 'vipb and isl1a', 'ZFS:0000035', 'ZFA:0000029', 'ZFA:0007126'),
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
  ('znf385d_magenta_6dpf_20250204_emb4_ventral_mVII r7_(RGB)', 'ZDB-GENE-130201-2', 'znf385d', 'ZFS:0000038', 'ZFA:0000029', 'ZFA:0007129');
