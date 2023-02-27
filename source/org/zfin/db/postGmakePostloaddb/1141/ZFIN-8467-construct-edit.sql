--liquibase formatted sql
--changeset rtaylor:ZFIN-8467-construct-edit.sql


UPDATE construct SET construct_name = 'Tg(6xUAS:EMTB-3xGFP)' WHERE construct_zdb_id = 'ZDB-TGCONSTRCT-141218-7';

DELETE FROM construct_component WHERE cc_construct_zdb_id = 'ZDB-TGCONSTRCT-141218-7';

INSERT INTO construct_component (cc_construct_zdb_id, cc_component_type, cc_component_category, cc_component_zdb_id, cc_component, cc_cassette_number, cc_order) VALUES
('ZDB-TGCONSTRCT-141218-7', 'text component', 'construct wrapper component', NULL, 'Tg', 1, 1),
('ZDB-TGCONSTRCT-141218-7', 'controlled vocab component', 'construct wrapper component', 'ZDB-CV-150506-7', '(', 1, 2),
('ZDB-TGCONSTRCT-141218-7', 'text component', 'promoter component', NULL, '6x', 1, 3),
('ZDB-TGCONSTRCT-141218-7', 'promoter of', 'promoter component', 'ZDB-EREGION-070122-1', 'UAS', 1, 4),
('ZDB-TGCONSTRCT-141218-7', 'controlled vocab component', 'promoter component', 'ZDB-CV-150506-10', ':', 1, 5),
('ZDB-TGCONSTRCT-141218-7', 'coding sequence of', 'coding component', 'ZDB-EREGION-141218-2', 'EMTB', 1, 6),
('ZDB-TGCONSTRCT-141218-7', 'text component', 'coding component', NULL, '-', 1, 7),
('ZDB-TGCONSTRCT-141218-7', 'text component', 'coding sequence component', NULL, '3x', 1, 8),
('ZDB-TGCONSTRCT-141218-7', 'coding sequence of', 'coding component', 'ZDB-EFG-070117-2', 'GFP', 1, 9),
('ZDB-TGCONSTRCT-141218-7', 'controlled vocab component', 'construct wrapper component', 'ZDB-CV-150506-8', ')', 1, 10);



SELECT regen_construct_marker ('ZDB-TGCONSTRCT-141218-7');
