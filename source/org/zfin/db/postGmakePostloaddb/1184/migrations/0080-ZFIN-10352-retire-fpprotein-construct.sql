--liquibase formatted sql

-- ZFIN-10352 follow-up: retire fpProtein_construct, the last stale denormalized cache in
-- the fluorescence chain, for the same reason fluorescent_marker was dropped in migration
-- 0050 -- it was a one-time backfill that no writer maintained.
--
-- fpProtein_construct was populated exactly once, by changeset 1124 (add_colors.sql), which
-- derived construct -> protein by joining fpProtein_efg to marker_relationship on
-- 'coding sequence of'. Nothing has written it since: linking an EFG to an FPBase protein
-- (EfgAPIController.createFPBaseAssociation) inserts into fpProtein_efg only, and
-- create_color_info() just recomputes colors on fluorescent_protein. So every construct
-- created after that backfill was silently colorless -- 1546 of them by the time this was
-- found, essentially every construct from 2021 on.
--
-- Every consumer now derives from the live chain
-- (construct -('coding sequence of')-> EFG -> fpProtein_efg -> fluorescent_protein), which
-- is what the construct search facets already did (construct_reporter_color et al.):
--   * construct page  -> Marker.getCodingSequenceFluorescentProteins(), feeding
--                        getFluorescentMarkers() / construct-view-summary.jsp
--   * construct API   -> HibernateMarkerRepository.getAllFluorescentConstructs() (rebuilt to
--                        walk MarkerRelationship), and FluorescentMarkerDTO.getProteins()
--   * DIH reindex     -> the marker-color union in fish / expression / feature sub-entities
--                        (db-data-config.xml) repointed to marker_relationship
--   * reverse mapping -> FluorescentProtein.constructs dropped; nothing read it
-- Nothing maps or queries the table any more, so it can go. The uniqueness constraint added
-- in migration 0040 goes with it.

--changeset rtaylor:0080-drop-fpprotein-construct-table
drop table if exists fpProtein_construct;
