--liquibase formatted sql
--changeset pm:ZFIN-6006


update sequence_feature_chromosome_location set sfcl_end_position=sfcl_start_position where sfcl_end_position!=sfcl_start_position;