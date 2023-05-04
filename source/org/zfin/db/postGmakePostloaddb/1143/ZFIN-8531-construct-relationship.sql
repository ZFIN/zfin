--liquibase formatted sql
--changeset rtaylor:ZFIN-8531-construct-relationship.sql

-- ZDB-TGCONSTRCT-171204-1 - Tg(rho:SFtag-whrna,myl7:EGFP) - construct
-- ZDB-EFG-070117-1 (EGFP) - coding sequence of
-- ZDB-GENE-991019-3 (myl7) - promoter of

-- EGFP
 -- construct_marker_relationship
 INSERT INTO zdb_active_data values ('ZDB-CMREL-230408-1');
 INSERT INTO construct_marker_relationship (conmrkrrel_zdb_id, conmrkrrel_construct_zdb_id, conmrkrrel_mrkr_zdb_id, conmrkrrel_relationship_type)
  VALUES ('ZDB-CMREL-230408-1', 'ZDB-TGCONSTRCT-171204-1', 'ZDB-EFG-070117-1', 'coding sequence of');

 -- marker_relationship
 INSERT INTO zdb_active_data values ('ZDB-MREL-230408-1');
 INSERT INTO marker_relationship (mrel_zdb_id, mrel_type, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_comments)
  VALUES ('ZDB-MREL-230408-1', 'coding sequence of', 'ZDB-TGCONSTRCT-171204-1', 'ZDB-EFG-070117-1', NULL);
 INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_significance, recattrib_source_type, recattrib_created_at, recattrib_modified_at, recattrib_modified_count)
  VALUES ('ZDB-MREL-230408-1', 'ZDB-PUB-220706-16', NULL, 'standard', NULL, NULL, NULL);


-- myl7
 -- construct_marker_relationship
 INSERT INTO zdb_active_data values ('ZDB-CMREL-230408-2');
 INSERT INTO construct_marker_relationship (conmrkrrel_zdb_id, conmrkrrel_construct_zdb_id, conmrkrrel_mrkr_zdb_id, conmrkrrel_relationship_type)
  VALUES ('ZDB-CMREL-230408-2', 'ZDB-TGCONSTRCT-171204-1', 'ZDB-GENE-991019-3', 'promoter of');

 -- marker_relationship
 INSERT INTO zdb_active_data values ('ZDB-MREL-230408-2');
 INSERT INTO marker_relationship (mrel_zdb_id, mrel_type, mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, mrel_comments)
  VALUES ('ZDB-MREL-230408-2', 'promoter of', 'ZDB-TGCONSTRCT-171204-1', 'ZDB-GENE-991019-3', NULL);
 INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_significance, recattrib_source_type, recattrib_created_at, recattrib_modified_at, recattrib_modified_count)
  VALUES ('ZDB-MREL-230408-2', 'ZDB-PUB-220706-16', NULL, 'standard', NULL, NULL, NULL);

