--liquibase formatted sql
--changeset pkalita:CUR-809

ALTER TABLE pub_tracking_location
    ADD COLUMN ptl_display_order int NOT NULL DEFAULT 9999;

UPDATE pub_tracking_location
SET ptl_display_order = 1
WHERE ptl_location = 'BIN_1';

UPDATE pub_tracking_location
SET ptl_display_order = 2
WHERE ptl_location = 'BIN_2';

UPDATE pub_tracking_location
SET ptl_display_order = 3
WHERE ptl_location = 'NEW_PHENO';

UPDATE pub_tracking_location
SET ptl_display_order = 4
WHERE ptl_location = 'NEW_EXPR';

UPDATE pub_tracking_location
SET ptl_display_order = 5
WHERE ptl_location = 'ORTHO';

-- disease will be 6

UPDATE pub_tracking_location
SET ptl_display_order = 7
WHERE ptl_location = 'BIN_3';

UPDATE pub_tracking_location
SET ptl_display_order = 101
WHERE ptl_location = 'pub_indexer_1';

UPDATE pub_tracking_location
SET ptl_display_order = 102
WHERE ptl_location = 'pub_indexer_2';

UPDATE pub_tracking_location
SET ptl_display_order = 103
WHERE ptl_location = 'pub_indexer_3';

INSERT INTO pub_tracking_location (ptl_location, ptl_location_display, ptl_role, ptl_location_definition, ptl_display_order)
VALUES ('DISEASE', 'Disease', 'curator', 'Papers with disease data', 6);