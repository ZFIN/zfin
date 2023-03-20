--liquibase formatted sql
--changeset rtaylor:ZFIN-8471-construct-edit.sql

UPDATE construct SET construct_name = 'Tg(actb2:EMTB-3xEGFP)' WHERE construct_zdb_id = 'ZDB-TGCONSTRCT-110329-1';

DELETE FROM construct_component WHERE cc_construct_zdb_id = 'ZDB-TGCONSTRCT-110329-1';

INSERT INTO construct_component ("cc_construct_zdb_id", "cc_component_type", "cc_component_category", "cc_component_zdb_id", "cc_component", "cc_cassette_number", "cc_order") VALUES ('ZDB-TGCONSTRCT-110329-1', 'text component', 'construct wrapper component', NULL, 'Tg', 1, 1);
INSERT INTO construct_component ("cc_construct_zdb_id", "cc_component_type", "cc_component_category", "cc_component_zdb_id", "cc_component", "cc_cassette_number", "cc_order") VALUES ('ZDB-TGCONSTRCT-110329-1', 'controlled vocab component', 'construct wrapper component', 'ZDB-CV-150506-7', '(', 1, 2);
INSERT INTO construct_component ("cc_construct_zdb_id", "cc_component_type", "cc_component_category", "cc_component_zdb_id", "cc_component", "cc_cassette_number", "cc_order") VALUES ('ZDB-TGCONSTRCT-110329-1', 'promoter of', 'promoter component', 'ZDB-GENE-000329-3', 'actb2', 1, 3);
INSERT INTO construct_component ("cc_construct_zdb_id", "cc_component_type", "cc_component_category", "cc_component_zdb_id", "cc_component", "cc_cassette_number", "cc_order") VALUES ('ZDB-TGCONSTRCT-110329-1', 'controlled vocab component', 'promoter component', 'ZDB-CV-150506-10', ':', 1, 4);
INSERT INTO construct_component ("cc_construct_zdb_id", "cc_component_type", "cc_component_category", "cc_component_zdb_id", "cc_component", "cc_cassette_number", "cc_order") VALUES ('ZDB-TGCONSTRCT-110329-1', 'coding sequence of', 'coding component', 'ZDB-EREGION-141218-2', 'EMTB', 1, 5);
INSERT INTO construct_component ("cc_construct_zdb_id", "cc_component_type", "cc_component_category", "cc_component_zdb_id", "cc_component", "cc_cassette_number", "cc_order") VALUES ('ZDB-TGCONSTRCT-110329-1', 'text component', 'coding component', NULL, '-', 1, 6);
INSERT INTO construct_component ("cc_construct_zdb_id", "cc_component_type", "cc_component_category", "cc_component_zdb_id", "cc_component", "cc_cassette_number", "cc_order") VALUES ('ZDB-TGCONSTRCT-110329-1', 'text component', 'coding component', NULL, '3x', 1, 7);
INSERT INTO construct_component ("cc_construct_zdb_id", "cc_component_type", "cc_component_category", "cc_component_zdb_id", "cc_component", "cc_cassette_number", "cc_order") VALUES ('ZDB-TGCONSTRCT-110329-1', 'coding sequence of', 'coding component', 'ZDB-EFG-070117-1', 'EGFP', 1, 8);
INSERT INTO construct_component ("cc_construct_zdb_id", "cc_component_type", "cc_component_category", "cc_component_zdb_id", "cc_component", "cc_cassette_number", "cc_order") VALUES ('ZDB-TGCONSTRCT-110329-1', 'controlled vocab component', 'construct wrapper component', 'ZDB-CV-150506-8', ')', 1, 9);

INSERT INTO zdb_active_data values ('ZDB-MREL-230319-1');

INSERT INTO "public"."marker_relationship" ("mrel_zdb_id", "mrel_type", "mrel_mrkr_1_zdb_id", "mrel_mrkr_2_zdb_id", "mrel_comments")
VALUES ('ZDB-MREL-230319-1', 'coding sequence of', 'ZDB-TGCONSTRCT-110329-1', 'ZDB-EREGION-141218-2', NULL);

INSERT INTO "public"."record_attribution" ("recattrib_data_zdb_id", "recattrib_source_zdb_id", "recattrib_source_significance", "recattrib_source_type", "recattrib_created_at", "recattrib_modified_at", "recattrib_modified_count")
VALUES ('ZDB-MREL-230319-1', 'ZDB-PUB-110321-1', NULL, 'standard', NULL, NULL, NULL);

SELECT regen_construct_marker ('ZDB-TGCONSTRCT-110329-1');
