--liquibase formatted sql
--changeset xshao:CUR-874-c

update construct_component
   set cc_component = '-', cc_component_zdb_id = null
 where cc_construct_zdb_id = 'ZDB-TGCONSTRCT-160803-2'
   and cc_pk_id = 36521
   and cc_component = ':';
   
update construct_component
   set cc_component = ':', cc_component_zdb_id = null
 where cc_construct_zdb_id = 'ZDB-TGCONSTRCT-160803-2'
   and cc_pk_id = 36518
   and cc_component = '-';
   
update marker
   set mrkr_name = 'Tg(myog:Hsa.HIST1H2BJ-mRFP)'
 where mrkr_zdb_id = 'ZDB-TGCONSTRCT-160803-2';

update construct
   set construct_name = 'Tg(myog:Hsa.HIST1H2BJ-mRFP)'
 where construct_zdb_id = 'ZDB-TGCONSTRCT-160803-2';
