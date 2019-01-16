--liquibase formatted sql
--changeset pm:CUR-861b

update construct_component set cc_component_zdb_id='ZDB-EREGION-181119-1' where cc_component='E1b';
update construct_component set cc_component='E1B' where cc_component='E1b';


delete from construct_component where cc_component='ADV' and cc_construct_Zdb_id in (select construct1 from constructreln);
delete from construct_component where cc_component='.' and cc_construct_Zdb_id in (select construct1 from constructreln);
-- update construct_component set cc_order=16 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_order=14;
-- update construct_component set cc_order=15 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_order=13;
--
-- update construct_component set cc_order=16 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_order=14;
-- update construct_component set cc_order=15 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_order=13;
-- update construct_component set cc_order=14 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_order=12;
-- update construct_component set cc_order=13 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_order=11;
-- update construct_component set cc_order=12 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_order=10;
--
-- update construct_component set cc_order=7 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_order=8;
-- update construct_component set cc_order=6 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_order=7;*/
--
-- update construct_component set cc_order=4 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_component='UAS';
-- update construct_component set cc_order=5 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_component='-' and cc_component_category='promoter component';
-- update construct_component set cc_order=7 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_component=':' ;
-- update construct_component set cc_order=11 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_component='NTR' ;
-- update construct_component set cc_order=13 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4' and cc_component='TagRFPT' ;
--
--
-- insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
--  values ('ZDB-TGCONSTRCT-141113-4','text component','promoter component','14x',1,3);
--
-- insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
--  values ('ZDB-TGCONSTRCT-141113-4','text component','coding sequence component','-',1,10);
--  insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
--  values ('ZDB-TGCONSTRCT-141113-4','text component','coding sequence component','Hbb2',1,9);
-- insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
--  values ('ZDB-TGCONSTRCT-141113-4','controlled vocab component ','coding sequence component','ZDB-CV-150506-25','Ocu.',1,8);
--
-- delete from construct_component where cc_order=12 and cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-4';
-- insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
--  values ('ZDB-TGCONSTRCT-141113-4','text component','coding sequence component','-',1,12);
--  insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
--  values ('ZDB-TGCONSTRCT-141113-4','text component','coding sequence component','-',1,14);




7


