--liquibase formatted sql
--changeset pm:ZFIN-6522
--delete records that were incorrectly put in thru the ensembl load(those associated with the expression atlas)

delete from sequence_feature_chromosome_location_generated where sfclg_fdb_db_id=91;
