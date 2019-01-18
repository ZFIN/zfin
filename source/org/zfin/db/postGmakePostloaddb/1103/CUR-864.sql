--liquibase formatted sql
--changeset pm:CUR-864

update construct_component set cc_component_zdb_id='ZDB-EREGION-181218-1' where cc_component='egr2b' and cc_construct_zdb_id='ZDB-TGCONSTRCT-101103-3';
update construct_component set cc_component='ELTC' where cc_component_zdb_id='ZDB-EREGION-181218-1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-101103-3';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EREGION-181218-1' where  conmrkrrel_construct_zdb_id = 'ZDB-TGCONSTRCT-101103-3' and conmrkrrel_mrkr_zdb_id='ZDB-GENE-980526-283';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EREGION-181218-1' where  mrel_mrkr_1_zdb_id = 'ZDB-TGCONSTRCT-101103-3' and mrel_mrkr_2_zdb_id='ZDB-GENE-980526-283';

update construct_component set cc_component_zdb_id='ZDB-EREGION-181218-2' where cc_component='ubc' and cc_construct_zdb_id='ZDB-TGCONSTRCT-160824-4';
update construct_component set cc_component='UBCI' where cc_component_zdb_id='ZDB-EREGION-181218-2' and cc_construct_zdb_id='ZDB-TGCONSTRCT-160824-4';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EREGION-181218-2' where  conmrkrrel_construct_zdb_id = 'ZDB-TGCONSTRCT-160824-4' and conmrkrrel_mrkr_zdb_id='ZDB-GENE-061110-88';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EREGION-181218-2' where  mrel_mrkr_1_zdb_id = 'ZDB-TGCONSTRCT-160824-4' and mrel_mrkr_2_zdb_id='ZDB-GENE-061110-88';

create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-101103-3', 'ZDB-TGCONSTRCT-101103-3', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-101103-3';
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-160824-4', 'ZDB-TGCONSTRCT-160824-4', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-160824-4';

update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;

insert into record_attribution(recattrib_data_Zdb_id,recattrib_source_zdb_id) select aliasid,'ZDB-PUB-151205-4' from tmp_dalias;
drop table tmp_dalias;








