--liquibase formatted sql
--changeset sierra:update_assembly.sql

update sequence_feature_chromosome_location
 set sfcl_assembly = 'GRCz11'
 where sfcl_assembly in ('11','GCz11','GCRz11','GRCZ11','GCRZ11','GTCz11','GRcz11','CRCz11');

update sequence_feature_chromosome_location
 set sfcl_assembly = 'GRCz10'
where sfcl_assembly in ('GRCz10,');

