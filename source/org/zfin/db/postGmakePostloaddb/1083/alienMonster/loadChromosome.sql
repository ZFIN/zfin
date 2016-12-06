--liquibase formatted sql
--changeset prita:loadChromosome

update sequence_feature_chromosome_location set sfcl_chromosome=(select distinct ftrChrom from sanger10Locations where sfcl_feature_zdb_id=ftrzdb) where exists (select  'x' from sanger10Locations where sfcl_feature_zdb_id=ftrzdb);
update sequence_feature_chromosome_location set sfcl_start_position=(select locStart from sanger10Locations where sfcl_feature_zdb_id =ftrzdb) where exists (select  'x' from sanger10Locations where sfcl_feature_zdb_id=ftrzdb);
update sequence_feature_chromosome_location set sfcl_assembly='GRCz10' where sfcl_feature_zdb_id in (select feature_zdb_id from feature,sanger10Locations where feature_abbrev=ftrAbbrev) and sfcl_assembly='Zv9';


 
