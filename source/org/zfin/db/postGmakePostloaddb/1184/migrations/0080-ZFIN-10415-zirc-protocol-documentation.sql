--liquibase formatted sql

-- ZFIN-10415: a second attachment bucket on each genotyping assay, for the
-- protocol document that describes how the assay is run. It renders directly
-- below the per-assay-type results bucket ("Annotated gel images",
-- "Chromatograms", ...) and is offered for every assay type.
--
-- Two buckets in one form means af_kind finally has to carry its weight.
-- The original schema constrained it to the four-kind matrix
-- (chromatogram / gel_image / result_image / melt_curve); M4.3 collapsed the
-- form to one slot and dropped both the CHECK and the NOT NULL
-- (zirc-relax-genotyping-assay-file-kind.xml), leaving every row NULL. This
-- brings the column back as the discriminator the DTO splits on, with the
-- two kinds the form now actually has:
--
--   assay_result -- whatever the per-assay-type bucket collects (gel image,
--                   chromatogram, annotated result image, melt curve). Which
--                   of those four it is remains a UI-label concern driven by
--                   ga_assay_type, not a stored distinction -- re-deriving it
--                   per row would just duplicate the assay type.
--   protocol_doc -- the ZFIN-10415 protocol documentation.
--
-- Every existing row predates the protocol bucket, so NULL backfills to
-- assay_result. NOT NULL is restored afterwards: a kind-less row would be
-- invisible in the form (it belongs to neither bucket), which is a silent
-- data-loss mode rather than a display bug.

--changeset zirc:zfin-10415-backfill-af-kind
UPDATE zirc.genotyping_assay_file
   SET af_kind = 'assay_result'
 WHERE af_kind IS NULL;

--changeset zirc:zfin-10415-constrain-af-kind
ALTER TABLE zirc.genotyping_assay_file
    ALTER COLUMN af_kind SET NOT NULL;

ALTER TABLE zirc.genotyping_assay_file
    DROP CONSTRAINT IF EXISTS genotyping_assay_file_af_kind_check;

ALTER TABLE zirc.genotyping_assay_file
    ADD CONSTRAINT genotyping_assay_file_af_kind_check
        CHECK (af_kind IN ('assay_result', 'protocol_doc'));
