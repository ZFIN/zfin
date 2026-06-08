--liquibase formatted sql
--changeset rtaylor:ZFIN-9940-fix-guest-attribution-submitter

-- The CheckFeatureGeneAttributionTask was run with COMMIT_CHANGES=true during the
-- release 1176 post-load (2025-12-16). Because the task runs without a logged-in user,
-- every history row it wrote was attributed to the anonymous "Guest" fallback
-- (submitter_id NULL, submitter_name 'Guest'). Re-attribute those ~8.4k rows to the
-- developer who ran the task, Ryan Taylor (ZDB-PERS-210917-1), and note the ticket.
UPDATE updates
SET submitter_id   = 'ZDB-PERS-210917-1',
    submitter_name = 'Taylor, Ryan',
    comments       = comments || ' (See ZFIN-9940)'
WHERE submitter_name = 'Guest'
  AND field_name = 'record attribution'
  AND comments = 'Added direct attribution to gene related to feature';
