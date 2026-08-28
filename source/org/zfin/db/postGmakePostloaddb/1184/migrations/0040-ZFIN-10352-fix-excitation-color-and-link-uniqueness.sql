--liquibase formatted sql

-- ZFIN-10352 adjacent fixes surfaced while reworking the fluorescence tables.
--
-- The create_color_info() bug (it computed fp_excitation_color from the EMISSION length) is
-- fixed in its canonical home, lib/DB_functions/create_color_info.sql, which is redeployed on
-- every build (gradle make -> deployPostgresFunctions) ahead of these postBuild migrations.
-- Here we just (1) backfill existing rows through the now-corrected function and (2) add the
-- link-table uniqueness guards.

-- (1) Backfill: recompute colors so existing (wrong) fp_excitation_color values are corrected.
--changeset rtaylor:0040-backfill-corrected-colors
select create_color_info();

-- (2) The EFG<->protein and construct<->protein link tables had no uniqueness guard, so
--     duplicate links were possible (callers relied on NOT EXISTS). No duplicates exist
--     today (verified), so add the constraint. Also makes fpProtein_efg a clean key for a
--     future db_link-derived rebuild (see ticket-efg-fpbase-shared-key.md).
--changeset rtaylor:0040-fpprotein-link-unique-constraints
alter table fpprotein_efg add constraint fpprotein_efg_uq unique (fe_mrkr_zdb_id, fe_fl_protein_id);
alter table fpprotein_construct add constraint fpprotein_construct_uq unique (fc_mrkr_zdb_id, fc_fl_protein_id);
