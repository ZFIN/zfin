--liquibase formatted sql
--changeset rtaylor:ZFIN-8726.sql

-- Fix problem where the record attribution table still refers to the original publication that has since been merged
-- Specifically we are looking at the record_attribution table where the source is ZDB-PUB-151203-1

-- Fix the problems
SELECT update_recattrib_after_pub_withdrawn('ZDB-PUB-151203-1', 'ZDB-PUB-111129-1');
SELECT update_recattrib_after_pub_withdrawn('ZDB-PUB-170217-10', 'ZDB-PUB-000831-4');
SELECT update_recattrib_after_pub_withdrawn('ZDB-PUB-090225-18', 'ZDB-PUB-060906-3');
SELECT update_recattrib_after_pub_withdrawn('ZDB-PUB-140325-6', 'ZDB-PUB-140606-4');
SELECT update_recattrib_after_pub_withdrawn('ZDB-PUB-010220-6', 'ZDB-PUB-170218-14');
SELECT update_recattrib_after_pub_withdrawn('ZDB-PUB-170217-11', 'ZDB-PUB-980313-2');

-- remaining issues:
-- 14682625	ZDB-MIRNAG-141229-2	ZDB-PUB-14052-12
-- 10405695	ZDB-GENE-141031-1	ZDB-PUB-140101-7
DELETE FROM record_attribution
WHERE recattrib_source_zdb_id IN ('ZDB-PUB-140101-7', 'ZDB-PUB-14052-12')
  AND recattrib_source_zdb_id NOT IN (SELECT zdb_id FROM publication);
