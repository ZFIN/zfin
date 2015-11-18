--liquibase formatted sql

--changeset pkalita:case-13657

CREATE TABLE old_sfcl (
  sfcl_chromosome VARCHAR(20),
  sfcl_data_zdb_id VARCHAR(50),
  sfcl_pk_id INTEGER,
  sfcl_acc_num VARCHAR(30),
  sfcl_start INTEGER,
  sfcl_end INTEGER,
  sfcl_location_source VARCHAR(40),
  sfcl_location_subsource VARCHAR(100),
  sfcl_fdb_db_id INTEGER
);

LOAD FROM '/research/zunloads/release_scripts/1073/island/2015.10.25.1_sequence_feature_chromosome_location'
  INSERT INTO old_sfcl;


UPDATE old_sfcl
SET sfcl_location_source = 'ZfinGbrowseZv9StartEndLoader'
WHERE sfcl_location_source = 'ZfinGbrowseStartEndLoader';

INSERT INTO sequence_feature_chromosome_location
SELECT * FROM old_sfcl
WHERE sfcl_location_source = 'ZfinGbrowseZv9StartEndLoader';
drop table old_sfcl;
