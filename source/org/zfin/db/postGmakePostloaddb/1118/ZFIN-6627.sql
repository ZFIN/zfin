--liquibase formatted sql
--changeset pm:ZFIN-6627.sql

update sequence_feature_chromosome_location_generated set sfclg_pub_zdb_id ='ZDB-PUB-121121-1' where sfclg_location_subsource='BurgessLin';




