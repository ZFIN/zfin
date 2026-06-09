--liquibase formatted sql
--changeset rtaylor:ZFIN-10297-merge-person

-- Merging duplicate person records (ZFIN-10297)
-- Keeping  ZDB-PERS-000914-9 (Ward, Alister C.)
-- Removing ZDB-PERS-120209-1 (Ward, Alister)

-- Reassign the duplicate's publication associations to the surviving person.
-- The duplicate's int_person_pub rows include ZDB-PUB-141005-5, which the
-- surviving person is already linked to; reassigning that row would violate the
-- (source_id, target_id) primary key, so we skip any target the surviving
-- person already holds. The skipped duplicate row is removed by the ON DELETE
-- CASCADE when the zdb_active_source row is deleted below.
UPDATE int_person_pub
   SET source_id = 'ZDB-PERS-000914-9'
 WHERE source_id = 'ZDB-PERS-120209-1'
   AND target_id NOT IN (
       SELECT target_id FROM int_person_pub WHERE source_id = 'ZDB-PERS-000914-9'
   );

-- Reassign the correspondence-recipient row (FK is ON DELETE RESTRICT).
UPDATE pub_correspondence_recipient
   SET pubcr_recipient_person_zdb_id = 'ZDB-PERS-000914-9'
 WHERE pubcr_recipient_person_zdb_id = 'ZDB-PERS-120209-1';

-- Reassign the duplicate's audit-trail rows to the surviving record.
UPDATE updates
   SET rec_id = 'ZDB-PERS-000914-9'
 WHERE rec_id = 'ZDB-PERS-120209-1';

-- Record the merge.
INSERT INTO updates ("submitter_id", "rec_id", "field_name", "new_value", "old_value", "comments", "submitter_name")
  VALUES ('ZDB-PERS-210917-1', 'ZDB-PERS-120209-1', 'zdb_id', 'ZDB-PERS-000914-9', 'ZDB-PERS-120209-1', 'Merged duplicate person record', 'Taylor, Ryan');

-- Remove the duplicate person. ON DELETE CASCADE from zdb_active_source removes
-- the person row, its zdb_submitters login, and any remaining int_person_pub row.
DELETE FROM zdb_active_source WHERE zactvs_zdb_id = 'ZDB-PERS-120209-1';
