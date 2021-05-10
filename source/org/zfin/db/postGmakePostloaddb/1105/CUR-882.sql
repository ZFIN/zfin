--liquibase formatted sql
--changeset pm:CUR-882


create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-160413-3', 'ZDB-TGCONSTRCT-160413-3', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-160413-3';


update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;

insert into record_attribution(recattrib_data_Zdb_id,recattrib_source_zdb_id) select aliasid,'ZDB-PUB-130416-10' from tmp_dalias;
drop table tmp_dalias;

update construct set construct_name='Tg(HSE:fhl1b,GFP)' where construct_zdb_id='ZDB-TGCONSTRCT-160413-3';
update marker set mrkr_name='Tg(HSE:fhl1b,GFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-160413-3';
update marker set mrkr_abbrev ='Tg(HSE:fhl1b,GFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-160413-3';

update construct_component set cc_component_zdb_id='ZDB-EREGION-121024-3' where cc_component='hsp70l' and cc_construct_zdb_id='ZDB-TGCONSTRCT-160413-3';
update construct_component set cc_component='HSE' where cc_component_zdb_id='ZDB-EREGION-121024-3' and cc_construct_zdb_id='ZDB-TGCONSTRCT-160413-3';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EREGION-121024-3' where  conmrkrrel_construct_zdb_id = 'ZDB-TGCONSTRCT-160413-3' and conmrkrrel_mrkr_zdb_id='ZDB-GENE-050321-1';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EREGION-121024-3' where  mrel_mrkr_1_zdb_id = 'ZDB-TGCONSTRCT-160413-3' and mrel_mrkr_2_zdb_id='ZDB-GENE-050321-1';













