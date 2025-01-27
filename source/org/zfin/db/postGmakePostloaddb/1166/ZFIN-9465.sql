--liquibase formatted sql
--changeset rtaylor:ZFIN-9465.sql

-- Merging duplicate lab and duplicate person
-- We are keeping ZDB-LAB-230309-1 and ZDB-PERS-230309-1
-- We are deleting ZDB-LAB-210312-1 and ZDB-PERS-160817-1


UPDATE pub_correspondence_recipient
SET pubcr_recipient_person_zdb_id = 'ZDB-PERS-230309-1'
WHERE pubcr_recipient_person_zdb_id = 'ZDB-PERS-160817-1';

INSERT INTO updates ("submitter_id", "rec_id", "field_name", "new_value", "old_value", "comments", "submitter_name")
  VALUES ('ZDB-PERS-210917-1', 'ZDB-PERS-160817-1', 'zdb_id', 'ZDB-PERS-230309-1', 'ZDB-PERS-160817-1', 'Merged duplicate person record', 'Taylor, Ryan');

DELETE FROM zdb_active_source WHERE zactvs_zdb_id = 'ZDB-PERS-160817-1';

INSERT INTO updates ("submitter_id", "rec_id", "field_name", "new_value", "old_value", "comments", "submitter_name")
  VALUES ('ZDB-PERS-210917-1', 'ZDB-LAB-210312-1', 'zdb_id', 'ZDB-LAB-230309-1', 'ZDB-LAB-210312-1', 'Merged duplicate lab record', 'Taylor, Ryan');

DELETE FROM zdb_active_source WHERE zactvs_zdb_id = 'ZDB-LAB-210312-1';

