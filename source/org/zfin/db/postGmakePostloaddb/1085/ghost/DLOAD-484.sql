--liquibase formatted sql
--changeset pm:DLOAD-484

UPDATE tmp_clone_alias
SET aliasid = get_id('DALIAS');

insert into zdb_active_Data (zactvd_zdb_id) select cloneid from tmp_clone_alias where cloneid not in (select zactvd_zdb_id from zdb_active_data);
insert into zdb_active_Data (zactvd_zdb_id) select aliasid from tmp_clone_alias;
insert into data_alias (dalias_zdb_id,dalias_alias,dalias_data_zdb_id,dalias_group_id) select distinct aliasid,clonealias,cloneid,1 from tmp_clone_alias;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id) select aliasid,'ZDB-PUB-040907-1' from tmp_clone_alias;
DROP TABLE tmp_clone_alias;

