--liquibase formatted sql
--changeset rtaylor:ZFIN-8520.sql

INSERT INTO pub_tracking_location ("ptl_location", "ptl_location_display", "ptl_role", "ptl_location_definition") VALUES
                                  ('DRUG', 'Drug', 'curator', ''),
                                  ('ENVIRONMENTAL_TOX', 'Environmental Tox', 'curator', ''),
                                  ('NANOMATERIALS', 'Nanomaterials', 'curator', ''),
                                  ('NATURAL_PRODUCT', 'Natural Product', 'curator', ''),
                                  ('XENOGRAFT', 'Xenograft', 'curator', '');

update pub_tracking_location set ptl_display_order = 1 where ptl_location = 'BIN_1';
update pub_tracking_location set ptl_display_order = 2 where ptl_location = 'NEW_PHENO';
update pub_tracking_location set ptl_display_order = 3 where ptl_location = 'NEW_EXPR';
update pub_tracking_location set ptl_display_order = 4 where ptl_location = 'ORTHO';
update pub_tracking_location set ptl_display_order = 5 where ptl_location = 'DISEASE';
update pub_tracking_location set ptl_display_order = 6 where ptl_location = 'DRUG';
update pub_tracking_location set ptl_display_order = 7 where ptl_location = 'XENOGRAFT';
update pub_tracking_location set ptl_display_order = 8 where ptl_location = 'ENVIRONMENTAL_TOX';
update pub_tracking_location set ptl_display_order = 9 where ptl_location = 'NANOMATERIALS';
update pub_tracking_location set ptl_display_order = 10 where ptl_location = 'NATURAL_PRODUCT';
update pub_tracking_location set ptl_display_order = 11 where ptl_location = 'BIN_2';
update pub_tracking_location set ptl_display_order = 12 where ptl_location = 'BIN_3';
update pub_tracking_location set ptl_display_order = 13 where ptl_location = 'ZEBRASHARE';
