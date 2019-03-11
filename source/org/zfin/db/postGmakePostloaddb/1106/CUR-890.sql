--liquibase formatted sql
--changeset pm:CUR-890


create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-181119-2', 'ZDB-TGCONSTRCT-181119-2', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-181119-2';
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-181119-1', 'ZDB-TGCONSTRCT-181119-1', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-181119-1';

update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;

update construct set construct_name='Tg(-3mnx1:EGFP-Hsa.TARDBP)' where construct_zdb_id='ZDB-TGCONSTRCT-181119-2';
update marker set mrkr_name='Tg(-3mnx1:EGFP-Hsa.TARDBP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-181119-2';
update marker set mrkr_abbrev ='Tg(-3mnx1:EGFP-Hsa.TARDBP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-181119-2';

update construct_component set cc_component_zdb_id=null where cc_component='tardbp' and cc_construct_zdb_id='ZDB-TGCONSTRCT-181119-2';
update construct_component set cc_component='Hsa.TARDBP' where cc_component='tardbp' and cc_construct_zdb_id='ZDB-TGCONSTRCT-181119-2';

delete from construct_marker_relationship where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-181119-2' and conmrkrrel_mrkr_zdb_id='ZDB-GENE-030131-3777';
delete from marker_relationship where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-181119-2' and mrel_mrkr_2_zdb_id='ZDB-GENE-030131-3777';


update construct set construct_name='Tg(-3mnx1:KALTA4-4xUAS-E1B-Hsa.HIST2H2BE-mCerulean3-P2A-mKOFP2-CAAX)' where construct_zdb_id='ZDB-TGCONSTRCT-181119-1';
update marker set mrkr_name='Tg(-3mnx1:KALTA4-4xUAS-E1B-Hsa.HIST2H2BE-mCerulean3-P2A-mKOFP2-CAAX)' where mrkr_zdb_id='ZDB-TGCONSTRCT-181119-1';
update marker set mrkr_abbrev ='Tg(-3mnx1:KALTA4-4xUAS-E1B-Hsa.HIST2H2BE-mCerulean3-P2A-mKOFP2-CAAX)' where mrkr_zdb_id='ZDB-TGCONSTRCT-181119-1';

update construct_component set cc_component_zdb_id=null where cc_component='H2B' and cc_construct_zdb_id='ZDB-TGCONSTRCT-181119-1';
update construct_component set cc_component='Hsa.HIST2H2BE' where cc_component='H2B' and cc_construct_zdb_id='ZDB-TGCONSTRCT-181119-1';




insert into record_attribution(recattrib_data_Zdb_id,recattrib_source_zdb_id) select aliasid,'ZDB-PUB-180627-10' from tmp_dalias;
drop table tmp_dalias;








