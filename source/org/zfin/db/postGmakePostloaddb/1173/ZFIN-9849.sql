--liquibase formatted sql
--changeset cmpich:ZFIN-9849

-- remove line designation 'hsc' from the Dowling lab
delete from source_feature_prefix where sfp_prefix_id = 50 and sfp_source_zdb_id = 'ZDB-LAB-970501-1';
