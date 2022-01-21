--liquibase formatted sql
--changeset cmpich:statistic-publication

-- ZDB-PUB-140325-6 has been merged into a different pub: ZDB-PUB-140606-4
-- remove old attributions.
DELETE FROM record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-140325-6';
