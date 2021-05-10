--liquibase formatted sql
--changeset pm:ZFIN-6405



create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-180615-5', 'ZDB-TGCONSTRCT-180615-5', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-180615-5';

update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;


update construct set construct_name='Tg(fabp10a:Hsa.AURKA_V352I,myl7:EGFP)' where construct_zdb_id='ZDB-TGCONSTRCT-180615-5';
update marker set mrkr_name='Tg(fabp10a:Hsa.AURKA_V352I,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-180615-5';
update marker set mrkr_abbrev ='Tg(fabp10a:Hsa.AURKA_V352I,myl7:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-180615-5';

update construct_component set cc_component='_V352I'  where cc_component='F1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-180615-5';
