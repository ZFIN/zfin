--liquibase formatted sql
--changeset pkalita:PUB-391

-- for pubs that are in Ready for Curation, currently not in any bin, put them in Bin 3 if they have a Toxicology and/or
-- Fish Orthology topic selected, but not any other topics. As of 30 June 2017 this affects 994 pubs.
UPDATE pub_tracking_history
SET pth_location_id = 6
WHERE pth_status_is_current = 't'
AND pth_pub_zdb_id IN (
  SELECT DISTINCT(pth_pub_zdb_id)
  FROM pub_tracking_history
  WHERE pth_status_is_current = 't'
  AND pth_status_id = 4
  AND pth_location_id IS NULL
  AND pth_pub_zdb_id NOT IN (
    SELECT DISTINCT cur_pub_zdb_id FROM curation
    WHERE cur_topic != 'Toxicology'
    AND cur_topic != 'Linked Authors'
    AND cur_topic != 'Fish Orthology'
    AND cur_data_found = 't'
  )
  AND pth_pub_zdb_id IN (
    SELECT DISTINCT cur_pub_zdb_id FROM curation
    WHERE (cur_topic = 'Toxicology' OR cur_topic = 'Fish Orthology')
    AND cur_data_found = 't'
  )
);
