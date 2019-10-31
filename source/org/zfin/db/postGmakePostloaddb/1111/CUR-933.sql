--liquibase formatted sql
--changeset pm:CUR-933



update construct set construct_name='Tg(Mmu.Dll4:EGFP)' where construct_zdb_id='ZDB-TGCONSTRCT-131112-1';
update marker set mrkr_name='Tg(Mmu.Dll4:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-131112-1';
update marker set mrkr_abbrev ='Tg(Mmu.Dll4:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-131112-1';

update construct_component set cc_order=7 where cc_order=6 and cc_construct_zdb_id='ZDB-TGCONSTRCT-131112-1';
update construct_component set cc_order=6 where cc_order=5 and cc_construct_zdb_id='ZDB-TGCONSTRCT-131112-1';
update construct_component set cc_order=5 where cc_order=4 and cc_construct_zdb_id='ZDB-TGCONSTRCT-131112-1';

update construct_component set cc_component_zdb_id='ZDB-CV-150506-12'  where cc_component='dll4' and cc_construct_zdb_id='ZDB-TGCONSTRCT-131112-1';
update construct_component set cc_component_type='controlled vocab component'  where cc_component='dll4' and cc_construct_zdb_id='ZDB-TGCONSTRCT-131112-1';
update construct_component set cc_component='Mmu.'  where cc_component='dll4' and cc_construct_zdb_id='ZDB-TGCONSTRCT-131112-1';

insert into construct_component(cc_construct_Zdb_id,cc_component,cc_order,cc_component_type,cc_component_category,cc_cassette_number) values('ZDB-TGCONSTRCT-131112-1','Dll4',4,'text component','promoter component',1);


delete from  construct_marker_relationship where   conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-131112-1' and conmrkrrel_mrkr_zdb_id='ZDB-GENE-041014-73';
delete from  marker_relationship where mrel_mrkr_2_zdb_id='ZDB-GENE-041014-73' and mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-131112-1';













