begin work;

create temp table sangerLocations(ftrAbbrev varchar(50),ftrAssembly varchar(10), ftrChrom varchar(2), locStart integer) with no log;


load from position.csv insert into sangerLocations;

create temp table tmp_id (zdb_id varchar(50), ftrAbbrev varchar(50),ftrAssembly varchar(10), ftrChrom varchar(2), locStart integer)
with no log;


insert into tmp_id (zdb_id, ftrAbbrev, ftrAssembly, ftrChrom, locStart)
 select get_id('SFCL'), ftrAbbrev, ftrAssembly, ftrChrom, locStart
   from sangerLocations;

insert into zdb_active_data
 select zdb_id from tmp_id;

insert into sequence_feature_chromosome_location (sfcl_zdb_id, sfcl_feature_zdb_id,sfcl_start_position,sfcl_end_position,sfcl_assembly,sfcl_chromosome) select zdb_id, feature_zdb_id, locStart,locStart,ftrAssembly,ftrChrom from tmp_id,feature where ftrChrom not like 'Zv%'  and ftrAbbrev=feature_abbrev;

insert into record_attribution (recattrib_data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select zdb_id,'ZDB-PUB-130425-4' ,'standard' from tmp_id;

rollback work;
--commit work;
 
