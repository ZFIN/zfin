--liquibase formatted sql
--changeset rtaylor:ZFIN-8470-construct-edit.sql


UPDATE marker_relationship
SET mrel_mrkr_2_zdb_id = 'ZDB-EFG-230215-1'
WHERE mrel_mrkr_2_zdb_id = 'ZDB-EFG-070409-1'
  and mrel_mrkr_1_zdb_id in
      (
       'ZDB-TGCONSTRCT-150424-8',
       'ZDB-TGCONSTRCT-160211-1',
       'ZDB-TGCONSTRCT-160122-2'
          );

