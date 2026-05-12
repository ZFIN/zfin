--liquibase formatted sql

-- Drop zirc.line_submission.ls_features_linked. The boolean was a
-- vestigial tri-state question on the submission form whose answer is
-- already implied by whether the dedicated `line_submission_linked_feature`
-- table has any rows for the submission. Removing avoids two
-- sources-of-truth and a curator-confusing "Features Linked?" radio
-- that no other UI or query branched on.
--
--changeset rtaylor:zirc-line-submission-drop-features-linked
ALTER TABLE zirc.line_submission DROP COLUMN IF EXISTS ls_features_linked;
