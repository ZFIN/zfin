--liquibase formatted sql
--changeset rtaylor:ZFIN-8726.sql


-- Fix problem where the record attribution table still refers to the original publication that has since been merged
-- Specifically we are looking at the record_attribution table where the source is ZDB-PUB-151203-1
DROP FUNCTION if exists change_recattrib_source_zdb_id;

CREATE OR REPLACE FUNCTION change_recattrib_source_zdb_id(old_id varchar, new_id varchar)
    RETURNS void AS '
    BEGIN
        -- create temporary table
        CREATE TEMP TABLE rec_to_add AS
        SELECT *
        FROM record_attribution
        WHERE recattrib_source_zdb_id = old_id;

        -- update the temporary table
        UPDATE rec_to_add
        SET recattrib_source_zdb_id = new_id
        WHERE recattrib_source_zdb_id = old_id;

        -- insert from the temporary table, ignoring duplicates
        INSERT INTO record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_significance,recattrib_source_type,recattrib_created_at,recattrib_modified_at,recattrib_modified_count)
        SELECT recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_significance,recattrib_source_type,recattrib_created_at,recattrib_modified_at,recattrib_modified_count
        FROM rec_to_add
        ON CONFLICT (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) DO NOTHING;

        -- delete from the main table
        DELETE FROM record_attribution WHERE recattrib_source_zdb_id = old_id;

        -- cleanup: drop the temporary table
        DROP TABLE rec_to_add;
    END;
' LANGUAGE plpgsql;

-- Fix the problems
SELECT change_recattrib_source_zdb_id('ZDB-PUB-151203-1', 'ZDB-PUB-111129-1');
SELECT change_recattrib_source_zdb_id('ZDB-PUB-170217-10', 'ZDB-PUB-000831-4');
SELECT change_recattrib_source_zdb_id('ZDB-PUB-090225-18', 'ZDB-PUB-060906-3');
SELECT change_recattrib_source_zdb_id('ZDB-PUB-140325-6', 'ZDB-PUB-140606-4');
SELECT change_recattrib_source_zdb_id('ZDB-PUB-010220-6', 'ZDB-PUB-170218-14');
SELECT change_recattrib_source_zdb_id('ZDB-PUB-170217-11', 'ZDB-PUB-980313-2');

-- cleanup: drop the function
DROP FUNCTION change_recattrib_source_zdb_id;

-- remaining issues:
-- 14682625	ZDB-MIRNAG-141229-2	ZDB-PUB-14052-12
-- 10405695	ZDB-GENE-141031-1	ZDB-PUB-140101-7
DELETE FROM record_attribution
WHERE recattrib_source_zdb_id IN ('ZDB-PUB-140101-7', 'ZDB-PUB-14052-12')
  AND recattrib_source_zdb_id NOT IN (SELECT zdb_id FROM publication);

-- delete empty record_attribution records
DELETE FROM record_attribution
WHERE recattrib_source_zdb_id = ''
   OR recattrib_source_zdb_id IS NULL;

