--liquibase formatted sql

-- Add the missing FK index on lslf_mutation_b_id. The original B4
-- migration created an index on lslf_mutation_a_id but not the
-- companion side — so deleting a Mutation referenced as B in any pair
-- requires a sequential scan of zirc.line_submission_linked_feature
-- to find the rows that need ON DELETE CASCADE to fire.

--changeset rtaylor:zirc-linked-feature-b-index
CREATE INDEX IF NOT EXISTS lslf_mutation_b_idx
    ON zirc.line_submission_linked_feature(lslf_mutation_b_id);
