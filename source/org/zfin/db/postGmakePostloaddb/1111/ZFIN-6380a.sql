--liquibase formatted sql
--changeset pm:ZFIN-6380a


/*create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-170424-2', 'ZDB-TGCONSTRCT-170424-2', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-170424-2';
insert into tmp_dalias (aliasid, consid ,consname) values('ZDB-TGCONSTRCT-170424-2', 'ZDB-TGCONSTRCT-170424-2', 'Tg(14xUAS:LOX2272-LOXP-RFP-LOX2272-CFP-LOXP-YFP)');


update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;
*/




update construct set construct_name='Tg(en.crest1-hsp70l:mKaede)' where construct_zdb_id='ZDB-TGCONSTRCT-170424-3';
update marker set mrkr_name='Tg(en.crest1-hsp70l:mKaede)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170424-3';
update marker set mrkr_abbrev ='Tg(en.crest1-hsp70l:mKaede)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170424-3';
update construct_component set cc_component_zdb_id='ZDB-ENHANCER-190923-1'  where cc_component='isl1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-170424-3';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-ENHANCER-190923-1' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-170424-3' and conmrkrrel_mrkr_zdb_id='ZDB-GENE-980526-112';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-ENHANCER-190923-1' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-170424-3' and mrel_mrkr_2_zdb_id='ZDB-GENE-980526-112';











