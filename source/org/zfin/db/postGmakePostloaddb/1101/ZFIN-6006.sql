--liquibase formatted sql
--changeset pm:ZFIN-6006


update sequence_feature_chromosome_location
 set sfcl_end_position=sfcl_start_position
 from feature
 where sfcl_end_position!=sfcl_start_position
  and sfcl_feature_zdb_id =feature_zdb_id
  and feature_type like '%POINT%';