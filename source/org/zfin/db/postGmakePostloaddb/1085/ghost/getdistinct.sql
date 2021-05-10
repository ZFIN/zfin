begin work;
create temp table frclone (alias varchar(50),calias varchar(50),cloneid varchar( 50)) with no log;
load from frload.unl insert into frclone;
unload to drclonedis.unl select distinct alias,calias,cloneid from frclone where cloneid is not null;
create temp table frclone1 (alias varchar(50),calias varchar(50),cloneid varchar(50)) with  no log;

load from drclonedis.unl insert into frclone1;
unload to notinactive select cloneid from frclone1 where cloneid not in (select zactvd_zdb_id from zdb_active_data);
update frclone1 set alias=get_id("DALIAS");
insert into zdb_active_data (zactvd_zdb_id) select alias from frclone1;
insert into data_alias (dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id,dalias_alias_lower) select alias,cloneid,calias,1,calias from frclone1; 
rollback work;
