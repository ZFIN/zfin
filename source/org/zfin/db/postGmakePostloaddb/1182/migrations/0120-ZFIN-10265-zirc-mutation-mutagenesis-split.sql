--liquibase formatted sql

-- Per the user-feedback PDF (more-product-specs/), what's currently a single
-- m_mutagenesis_protocol field is actually two distinct concepts:
--   • a "stage" — life stage at mutagenesis (adult females / adult males /
--     embryos / sperm)
--   • a "protocol" — method (CRISPR / EMS / ENU / g-rays / spontaneous /
--     TALEN / TMP / zinc finger nuclease)
-- Both are pull-downs with closed value sets; the protocol additionally
-- offers an "Other" free-text companion. We don't enforce the value list at
-- the DB level (those lists may grow), but split the column so each concept
-- has its own slot.

--changeset rtaylor:zirc-mutation-mutagenesis-split
ALTER TABLE zirc.mutation
    ADD COLUMN m_mutagenesis_stage          VARCHAR(50)  NULL,
    ADD COLUMN m_mutagenesis_protocol_other TEXT         NULL;
