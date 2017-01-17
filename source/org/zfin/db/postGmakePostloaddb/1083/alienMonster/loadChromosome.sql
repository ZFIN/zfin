--liquibase formatted sql
--changeset prita:loadChr

update sequence_feature_chromosome_location set sfcl_chromosome=(select distinct ftrChrom from tmp_sanger10location where sfcl_feature_zdb_id=ftrzdb) where exists (select  'x' from tmp_sanger10location where sfcl_feature_zdb_id=ftrzdb);
update sequence_feature_chromosome_location set sfcl_start_position=(select locStart from tmp_sanger10location where sfcl_feature_zdb_id =ftrzdb) where exists (select  'x' from tmp_sanger10location where sfcl_feature_zdb_id=ftrzdb);
update sequence_feature_chromosome_location set sfcl_assembly='GRCz10' where sfcl_feature_zdb_id in (select feature_zdb_id from feature,tmp_sanger10location where feature_abbrev=ftrAbbrev) and sfcl_assembly='Zv9';


 
