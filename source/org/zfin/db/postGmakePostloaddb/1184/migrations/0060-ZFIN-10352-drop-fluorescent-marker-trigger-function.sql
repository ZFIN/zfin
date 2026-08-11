--liquibase formatted sql

-- ZFIN-10352 follow-up: drop the orphaned trigger function left behind by the
-- fluorescent_marker retirement (migration 0050). Dropping the table dropped
-- fluorescent_marker_trigger with it, but not the PLPGSQL function it called;
-- lib/DB_triggers/fluorescent_marker.sql (which created both) is now deleted, so
-- nothing recreates either one.

--changeset rtaylor:0060-drop-fluorescent-marker-trigger-function
drop function if exists fluorescent_marker();
