--liquibase formatted sql
--changeset cmpich:statistic-publication

-- ZDB-PUB-140325-6 has been merged into a different pub: ZDB-PUB-140606-4
-- remove old attributions.
DELETE FROM record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-140325-6';

DELETE FROM record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-170217-11';

DELETE FROM record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-140101-7';

DELETE FROM record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-010220-6';

DELETE FROM record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-090225-18';

DELETE FROM record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-14052-12';

DELETE FROM record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-170217-10';

DELETE FROM record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-151203-1';
