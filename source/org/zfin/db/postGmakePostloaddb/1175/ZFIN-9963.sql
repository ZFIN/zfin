--liquibase formatted sql
--changeset cmpich:ZFIN-9963

-- add strand info to sequence_feature_chromosome_location_generated table

alter table sequence_feature_chromosome_location_generated
add column     sfclg_strand CHAR(1),
add COLUMN     sfclg_date_created TIMESTAMP WITH TIME ZONE DEFAULT NOW()
;