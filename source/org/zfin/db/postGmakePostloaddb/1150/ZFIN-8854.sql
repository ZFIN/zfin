--liquibase formatted sql
--changeset rtaylor:ZFIN-8854.sql

-- Add aliases from marker history table for some constructs
-- There should be about 296 aliases added

-- create the temp table based on data_alias
SELECT * INTO temp_to_load_data_alias FROM data_alias WHERE FALSE;

-- add the new aliases to the temp table
-- this gets every "alias" from the marker history table (mhist_mrkr_name_on_mhist_date)
-- it removes any that are already in the data_alias table (so we don't add duplicates)
-- it removes any that are the current name of the construct (so we don't add the current name as an alias)
-- what's left gets added to the temp table
INSERT INTO temp_to_load_data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_alias_lower, dalias_group_id)
SELECT
    get_id ('DALIAS'),
    mhist_mrkr_zdb_id,
    mhist_mrkr_name_on_mhist_date,
    lower(mhist_mrkr_name_on_mhist_date),
    1
FROM marker_history
WHERE mhist_mrkr_zdb_id LIKE '%CONSTRCT%'
  AND mhist_event = 'renamed'
  AND (mhist_mrkr_zdb_id, mhist_mrkr_name_on_mhist_date)              -- filter out existing aliases
    NOT IN (SELECT dalias_data_zdb_id, dalias_alias FROM data_alias)
  AND (mhist_mrkr_zdb_id, mhist_mrkr_name_on_mhist_date)              -- filter out the current name from marker table
    NOT IN (SELECT mrkr_zdb_id, mrkr_name FROM marker WHERE mrkr_type LIKE '%CONSTRCT')
  AND (mhist_mrkr_zdb_id, replace(mhist_mrkr_name_on_mhist_date, ' ', ''))  -- filter out alias that differs from current name only by spaces
    NOT IN (SELECT mrkr_zdb_id, replace(mrkr_name, ' ', '') FROM marker WHERE mrkr_type LIKE '%CONSTRCT')    
ORDER BY
    mhist_mrkr_zdb_id,
    mhist_mrkr_name_on_mhist_date;

-- add the new IDs
INSERT INTO zdb_active_data (SELECT dalias_zdb_id FROM temp_to_load_data_alias);

-- add the new aliases
INSERT INTO data_alias (SELECT * FROM temp_to_load_data_alias);
