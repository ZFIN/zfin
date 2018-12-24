--liquibase formatted sql
--changeset pm:CUR-866

delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-150430-2' and cc_pk_id in (17576,28753,28752,28751,28754,28758,28755,28757);
update construct_component set cc_component_zdb_id='ZDB-GENE-030131-7347' where cc_component='mpeg1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150430-2' and cc_pk_id=17577;
update construct_component set cc_component_type='promoter of'  where cc_component='mpeg1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150430-2' and cc_pk_id=17577;
update construct_component set cc_component='mpeg1.1' where cc_component='mpeg1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150430-2' and cc_pk_id=17577;



update construct set construct_name='Tg(mpeg1.1:Dendra2)' where construct_zdb_id='ZDB-TGCONSTRCT-150430-2';
update marker set mrkr_name='Tg(mpeg1.1:Dendra2)' where mrkr_zdb_id='ZDB-TGCONSTRCT-150430-2';
update marker set mrkr_abbrev='Tg(mpeg1.1:Dendra2)' where mrkr_zdb_id='ZDB-TGCONSTRCT-150430-2';


update construct set construct_name=replace(construct_name,'E1b','E1B') where construct_name like '%E1b%';
update marker set mrkr_name=replace(mrkr_name,'E1b','E1B') where mrkr_name like '%E1b%' and mrkr_type like '%CONSTRCT%';
update marker set mrkr_abbrev=replace(mrkr_abbrev,'E1b','E1B') where mrkr_abbrev like '%E1b%' and mrkr_type like '%CONSTRCT%';


update construct set construct_name=replace(construct_name,'ADV.','') where construct_name like '%ADV%';
update marker set mrkr_name=replace(mrkr_name,'ADV.','') where mrkr_name like '%ADV%';
update marker set mrkr_abbrev=replace(mrkr_abbrev,'ADV.','') where mrkr_abbrev like '%ADV%';