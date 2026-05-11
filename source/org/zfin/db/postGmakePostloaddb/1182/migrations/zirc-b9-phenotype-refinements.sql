--liquibase formatted sql

-- B9 phenotype refinements, plus a per-gene "section complete" flag.
--
-- p_hpf_start / p_hpf_end: timing as either a single point (start, no
--   end) or a range (start + end). Replaces the single p_hours_post_
--   fertilization. The unit toggle (hpf vs dpf) lives on the client —
--   storage is always integer hpf, and dpf-entered values get
--   multiplied by 24 before commit.
--
-- p_zirc_image_permission: complement to the existing p_zfin_image_
--   permission. The PDF spec splits "Does ZIRC/ZFIN have the permission
--   to publish these images on the ZIRC or ZFIN websites?" into two
--   separate Yes/No questions, one per organization.
--
-- p_non_mendelian_comment: free-text note alongside p_non_mendelian_
--   percentage, per the PDF "Allow comment here also".
--
-- p_stage: kept; reinterpreted as a server-managed cache. The service
--   derives it from p_hpf_start via the STAGE table (stg_hours_start /
--   stg_hours_end). The client renders it read-only.
--
-- g_section_complete: optional curator-side flag per the PDF "New
--   Checkbox for Gene Section Complete".

--changeset rtaylor:zirc-b9-phenotype-refinements
ALTER TABLE zirc.phenotype
    DROP COLUMN p_hours_post_fertilization,
    ADD COLUMN p_hpf_start              INTEGER,
    ADD COLUMN p_hpf_end                INTEGER,
    ADD COLUMN p_zirc_image_permission  BOOLEAN,
    ADD COLUMN p_non_mendelian_comment  TEXT;

ALTER TABLE zirc.gene
    ADD COLUMN g_section_complete BOOLEAN;
