--liquibase formatted sql
--changeset pm:ZFIN-6179


create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);

insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-161017-2', 'ZDB-TGCONSTRCT-161017-2', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-161017-2';

update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;



update construct set construct_name='Tg(pvalb9:Hsa.CLRN1_c.144T>G-YFP)' where construct_zdb_id='ZDB-TGCONSTRCT-161017-2';
update marker set mrkr_name='Tg(pvalb9:Hsa.CLRN1_c.144T>G-YFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-161017-2';
update marker set mrkr_abbrev ='Tg(pvalb9:Hsa.CLRN1_c.144T>G-YFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-161017-2';


update construct_component set cc_component='CLRN1_c.144T>G' where cc_component='CLRN1_N48K' and cc_construct_zdb_id='ZDB-TGCONSTRCT-161017-2';




insert into record_attribution(recattrib_data_Zdb_id,recattrib_source_zdb_id) select aliasid,'ZDB-PUB-190102-5' from tmp_dalias;
drop table tmp_dalias;








