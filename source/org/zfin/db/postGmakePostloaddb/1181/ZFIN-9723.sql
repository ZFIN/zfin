--liquibase formatted sql
--changeset rtaylor:ZFIN-9723

-- ZFIN-9723: Fix feature sort order not updating when abbreviation changes
--
-- The feature_abbrev and feature_name AFTER UPDATE triggers tried to set
-- NEW.feature_abbrev_order and NEW.feature_name_order, but PostgreSQL
-- ignores row modifications in AFTER triggers. This left stale sort-order
-- values whenever a feature abbreviation or name was changed post-creation.
--
-- Fix: make the existing BEFORE INSERT trigger also fire on UPDATE so that
-- scrub_char and zero_pad are always applied. Clean up the AFTER triggers
-- to contain only side-effects (history logging, genotype name cascades).

-- Fix any existing stale sort-order values.
-- Trigger definitions are updated in lib/DB_triggers/ and applied via gradle make.
UPDATE feature
SET feature_abbrev_order = zero_pad(feature_abbrev)
WHERE feature_abbrev_order IS DISTINCT FROM zero_pad(feature_abbrev);

UPDATE feature
SET feature_name_order = zero_pad(feature_name)
WHERE feature_name_order IS DISTINCT FROM zero_pad(feature_name);
