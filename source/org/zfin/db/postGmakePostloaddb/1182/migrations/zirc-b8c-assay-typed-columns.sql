--liquibase formatted sql

-- B8c: extend zirc.genotyping_assay with the type-specific columns the
-- xlsx field matrix calls for (more-product-specs/field summary
-- genotyping assay.xlsx). The form picks an assay type first; only the
-- columns relevant to that type are surfaced.
--
-- Existing columns stay as-is — they cover the shared primer + PCR
-- product fields and the RFLP/dCAPS digest fields. New columns:
--
--   ga_sequencing_primer            — PCR + sequencing
--   ga_dcaps_mismatch_primer        — dCAPS
--   ga_wt_specific_primer           — ASA / KASP
--   ga_mut_specific_primer          — ASA / KASP
--   ga_common_primer                — ASA / KASP
--   ga_kasp_genomic_sequence        — KASP
--   ga_sslp_*                       — SSLP block (7 columns)
--   ga_*_files_available            — Y/N flags for the four kinds of
--                                     uploadable evidence (chromatograms,
--                                     gel images, result images, melt
--                                     curves). The actual file upload
--                                     backend is deferred per the plan.

--changeset rtaylor:zirc-b8c-assay-typed-columns
ALTER TABLE zirc.genotyping_assay
    ADD COLUMN ga_sequencing_primer          TEXT,
    ADD COLUMN ga_dcaps_mismatch_primer      TEXT,
    ADD COLUMN ga_wt_specific_primer         TEXT,
    ADD COLUMN ga_mut_specific_primer        TEXT,
    ADD COLUMN ga_common_primer              TEXT,
    ADD COLUMN ga_kasp_genomic_sequence      TEXT,
    ADD COLUMN ga_sslp_marker_name           TEXT,
    ADD COLUMN ga_sslp_distance              TEXT,
    ADD COLUMN ga_sslp_genomic_location      TEXT,
    ADD COLUMN ga_sslp_induced_background    TEXT,
    ADD COLUMN ga_sslp_outcrossed_background TEXT,
    ADD COLUMN ga_sslp_induced_pcr           TEXT,
    ADD COLUMN ga_sslp_outcrossed_pcr        TEXT,
    ADD COLUMN ga_chromatogram_files_available BOOLEAN,
    ADD COLUMN ga_gel_images_available         BOOLEAN,
    ADD COLUMN ga_result_images_available      BOOLEAN,
    ADD COLUMN ga_melt_curve_files_available   BOOLEAN;
