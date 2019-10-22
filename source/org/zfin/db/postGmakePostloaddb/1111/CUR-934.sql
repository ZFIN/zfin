--liquibase formatted sql
--changeset xshao:CUR-934

update construct_component
   set cc_order = 9
 where cc_pk_id = 4395;
 
update construct_component
   set cc_order = 8
 where cc_pk_id = 4398; 

update construct set construct_name='Tg(UAS:EGFP-Hsa.HRAS_G12V)' where construct_zdb_id='ZDB-TGCONSTRCT-090702-1';
update marker set mrkr_name='Tg(UAS:EGFP-Hsa.HRAS_G12V)' where mrkr_zdb_id='ZDB-TGCONSTRCT-090702-1';
update marker set mrkr_abbrev ='Tg(UAS:EGFP-Hsa.HRAS_G12V)' where mrkr_zdb_id='ZDB-TGCONSTRCT-090702-1';

insert into construct_component (cc_construct_zdb_id,cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-090702-1','controlled vocab component','coding sequence component','ZDB-CV-150506-13','Hsa.',1,7);

