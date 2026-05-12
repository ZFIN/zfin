--liquibase formatted sql

-- "Linked features" was originally a free-text varchar of feature names, with
-- per-row distance metadata. The form spec is more specific: it's a pairwise
-- linkage between two mutations submitted on the *same* submission (e.g.,
-- mutations aaa and ccc are linked). This changeset replaces the feature
-- text column with two FKs into zirc.mutation, plus a CHECK constraint that
-- enforces both distinctness (a != b) and symmetric dedup (a < b) — so
-- (mutation_5, mutation_3) and (mutation_3, mutation_5) can't both exist as
-- "different" relationships.
--
-- Existing rows are dropped (only test data in dev).

--changeset rtaylor:zirc-line-submission-linked-feature-pair
DELETE FROM zirc.line_submission_linked_feature;

ALTER TABLE zirc.line_submission_linked_feature
    DROP CONSTRAINT line_submission_linked_feature_pk,
    DROP COLUMN lslf_feature;

ALTER TABLE zirc.line_submission_linked_feature
    ADD COLUMN lslf_mutation_a_id BIGINT NOT NULL
        CONSTRAINT fk_lslf_mutation_a
        REFERENCES zirc.mutation(m_id) ON DELETE CASCADE,
    ADD COLUMN lslf_mutation_b_id BIGINT NOT NULL
        CONSTRAINT fk_lslf_mutation_b
        REFERENCES zirc.mutation(m_id) ON DELETE CASCADE,
    ADD CONSTRAINT line_submission_linked_feature_pk
        PRIMARY KEY (lslf_line_submission_id, lslf_mutation_a_id, lslf_mutation_b_id),
    ADD CONSTRAINT lslf_normalized_pair
        CHECK (lslf_mutation_a_id < lslf_mutation_b_id);
