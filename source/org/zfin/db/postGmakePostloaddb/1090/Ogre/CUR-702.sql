begin work;
insert into zdb_active_Data (zactvd_Zdb_id) values ('ZDB-CV-170920-1');
insert into controlled_vocabulary (cv_zdb_id,cv_term_name,cv_foreign_species,cv_name_definition) values ('ZDB-CV-170920-1','Gut.','flagellate algae','Guillardia theta');
update  construct_component set cc_component_zdb_id='ZDB-CV-170920-1' where cc_construct_Zdb_id='ZDB-TGCONSTRCT-170913-3' and cc_component='Gut.';
update  construct_component set cc_component_zdb_id='ZDB-CV-170920-1' where cc_construct_Zdb_id='ZDB-TGCONSTRCT-170913-4' and cc_component='Gut.';

commit work;
