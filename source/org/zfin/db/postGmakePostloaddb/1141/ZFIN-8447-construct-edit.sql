--liquibase formatted sql
--changeset rtaylor:ZFIN-8447-construct-edit.sql


UPDATE construct SET construct_name = 'Tg(rho:SFtag-whrna,myl7:EGFP)' WHERE construct_zdb_id = 'ZDB-TGCONSTRCT-171204-1';

DELETE FROM construct_component WHERE cc_construct_zdb_id = 'ZDB-TGCONSTRCT-171204-1';

INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'text component', 'construct wrapper component', NULL, 'Tg', 1, 1);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'controlled vocab component', 'construct wrapper component', 'ZDB-CV-150506-7', '(', 1, 2);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'text component', 'promoter component', NULL, 'rho', 1, 3);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'controlled vocab component', 'promoter component', 'ZDB-CV-150506-10', ':', 1, 4);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'text component', 'coding component', NULL, 'SFtag', 1, 5);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'text component', 'coding component', NULL, '-', 1, 6);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'coding sequence of', 'coding component', 'ZDB-GENE-091118-27', 'whrna', 1, 7);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'controlled vocab component', 'promoter component', 'ZDB-CV-150506-11', ',', 2, 8);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'promoter of', 'promoter component', 'ZDB-GENE-991019-3', 'myl7', 2, 9);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'controlled vocab component', 'promoter component', 'ZDB-CV-150506-10', ':', 2, 10);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'text component', 'coding component', NULL, 'EGFP', 2, 11);
INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES ('ZDB-TGCONSTRCT-171204-1', 'controlled vocab component', 'construct wrapper component', 'ZDB-CV-150506-8', ')', 2, 12);

SELECT regen_construct_marker ('ZDB-TGCONSTRCT-171204-1');
