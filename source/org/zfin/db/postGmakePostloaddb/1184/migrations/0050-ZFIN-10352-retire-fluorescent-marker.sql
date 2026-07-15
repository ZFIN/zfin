--liquibase formatted sql

-- ZFIN-10352: retire the fluorescent_marker table (a stale, unmaintained denormalized
-- cache of marker -> protein emission/excitation length+color). Every consumer now derives
-- from the live link chain (fpProtein_efg / fpProtein_construct -> fluorescent_protein ->
-- fluorescent_color):
--   * app readers  -> HibernateMarkerRepository.getAllFluorescentEfgs/Constructs (rebuilt)
--   * app writer   -> EfgAPIController (save(flMarker) dropped; the fpProtein_efg association
--                     add persists the real link)
--   * marker pages -> Marker.getFluorescentMarkers() derives from the protein associations
--   * DIH reindex  -> fish / expression / feature sub-entities repointed in db-data-config.xml
-- The FluorescentMarker Hibernate entity became a plain POJO, so nothing maps this table.
--
-- create_color_info() no longer references fluorescent_marker either; that lives in its
-- canonical home lib/DB_functions/create_color_info.sql (redeployed every build by
-- gradle make -> deployPostgresFunctions, ahead of these postBuild migrations), so by the
-- time this drop runs the deployed function no longer touches the table.

-- drop the table (no incoming FKs; the entity mapping is gone).
--changeset rtaylor:0050-drop-fluorescent-marker-table
drop table fluorescent_marker;
