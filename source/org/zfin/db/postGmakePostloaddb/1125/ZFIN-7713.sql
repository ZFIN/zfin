--liquibase formatted sql
--changeset christian:ZFIN-7713

update sequence_feature_chromosome_location
set sfcl_chromosome_reference_accession_number = 'NC_007122.7'
where sfcl_feature_zdb_id = 'ZDB-ALT-190125-4';

update sequence_feature_chromosome_location
set sfcl_chromosome_reference_accession_number = 'NC_007117.7'
where sfcl_feature_zdb_id = 'ZDB-ALT-040826-20';




