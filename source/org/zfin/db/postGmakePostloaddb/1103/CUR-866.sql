--liquibase formatted sql
--changeset pm:CUR-866

delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-150430-2' and cc_pk_id in (17576,28753,28752,28751,28754,28758,28755,28757);
update construct_component set cc_component_zdb_id='ZDB-GENE-030131-7347' where cc_component='mpeg1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150430-2' and cc_pk_id=17577;
update construct_component set cc_component_type='promoter of'  where cc_component='mpeg1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150430-2' and cc_pk_id=17577;
update construct_component set cc_component='mpeg1.1' where cc_component='mpeg1' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150430-2' and cc_pk_id=17577;









