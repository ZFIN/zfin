--liquibase formatted sql
--changeset pm:ZFIN-6273


create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-130816-3', 'ZDB-TGCONSTRCT-130816-3', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-130816-3';
insert into tmp_dalias (aliasid, consid ,consname) values('ZDB-TGCONSTRCT-130816-3', 'ZDB-TGCONSTRCT-130816-3', 'Tg(14xUAS:LOX2272-LOXP-RFP-LOX2272-CFP-LOXP-YFP)');


update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;

update construct set construct_name='Tg(14xUAS:LOX2272-LOXP-Tomato-LOX2272-Cerulean-LOXP-YFP)' where construct_zdb_id='ZDB-TGCONSTRCT-130816-3';
update marker set mrkr_name='Tg(14xUAS:LOX2272-LOXP-Tomato-LOX2272-Cerulean-LOXP-YFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-130816-3';
update marker set mrkr_abbrev ='Tg(14xUAS:LOX2272-LOXP-Tomato-LOX2272-Cerulean-LOXP-YFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-130816-3';


update construct_component set cc_component_zdb_id='ZDB-EFG-111118-1'  where cc_component='RFP' and cc_construct_zdb_id='ZDB-TGCONSTRCT-130816-3';
update construct_component set cc_component_zdb_id='ZDB-EFG-110824-3'  where cc_component='CFP' and cc_construct_zdb_id='ZDB-TGCONSTRCT-130816-3';

update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EFG-111118-1' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-130816-3' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-070117-3';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EFG-110824-3' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-130816-3' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-080201-1';

update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-111118-1' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-130816-3' and mrel_mrkr_2_zdb_id='ZDB-EFG-070117-3';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-110824-3' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-130816-3' and mrel_mrkr_2_zdb_id='ZDB-EFG-080201-1';


insert into record_attribution(recattrib_data_Zdb_id,recattrib_source_zdb_id) select aliasid,'ZDB-PUB-130709-52' from tmp_dalias;
drop table tmp_dalias;

create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-130816-1', 'ZDB-TGCONSTRCT-130816-1', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-130816-1';
insert into tmp_dalias (aliasid, consid ,consname) values('ZDB-TGCONSTRCT-130816-1', 'ZDB-TGCONSTRCT-130816-1', 'Tg(ubb:LOX2272-LOXP-RFP-LOX2272-CFP-LOXP-YFP)');


update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;

update construct set construct_name='Tg(ubb:LOX2272-LOXP-Tomato-LOX2272-Cerulean-LOXP-YFP)' where construct_zdb_id='ZDB-TGCONSTRCT-130816-1';
update marker set mrkr_name='Tg(ubb:LOX2272-LOXP-Tomato-LOX2272-Cerulean-LOXP-YFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-130816-1';
update marker set mrkr_abbrev ='Tg(ubb:LOX2272-LOXP-Tomato-LOX2272-Cerulean-LOXP-YFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-130816-1';


update construct_component set cc_component_zdb_id='ZDB-EFG-111118-1'  where cc_component='RFP' and cc_construct_zdb_id='ZDB-TGCONSTRCT-130816-1';
update construct_component set cc_component_zdb_id='ZDB-EFG-110824-3'  where cc_component='CFP' and cc_construct_zdb_id='ZDB-TGCONSTRCT-130816-1';

update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EFG-111118-1' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-130816-1' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-070117-3';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EFG-110824-3' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-130816-1' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-080201-1';

update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-111118-1' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-130816-1' and mrel_mrkr_2_zdb_id='ZDB-EFG-070117-3';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-110824-3' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-130816-1' and mrel_mrkr_2_zdb_id='ZDB-EFG-080201-1';


insert into record_attribution(recattrib_data_Zdb_id,recattrib_source_zdb_id) select aliasid,'ZDB-PUB-130709-52' from tmp_dalias;
drop table tmp_dalias;


create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-130816-2', 'ZDB-TGCONSTRCT-130816-2', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-130816-2';
insert into tmp_dalias (aliasid, consid ,consname) values('ZDB-TGCONSTRCT-130816-2', 'ZDB-TGCONSTRCT-130816-2', 'Tg(4xUAS:LOX2272-LOXP-RFP-LOX2272-CFP-LOXP-YFP)');


update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;

update construct set construct_name='Tg(4xUAS:LOX2272-LOXP-Tomato-LOX2272-Cerulean-LOXP-YFP)' where construct_zdb_id='ZDB-TGCONSTRCT-130816-2';
update marker set mrkr_name='Tg(4xUAS:LOX2272-LOXP-Tomato-LOX2272-Cerulean-LOXP-YFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-130816-2';
update marker set mrkr_abbrev ='Tg(4xUAS:LOX2272-LOXP-Tomato-LOX2272-Cerulean-LOXP-YFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-130816-2';


update construct_component set cc_component_zdb_id='ZDB-EFG-111118-1'  where cc_component='RFP' and cc_construct_zdb_id='ZDB-TGCONSTRCT-130816-2';
update construct_component set cc_component_zdb_id='ZDB-EFG-110824-3'  where cc_component='CFP' and cc_construct_zdb_id='ZDB-TGCONSTRCT-130816-2';

update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EFG-111118-1' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-130816-2' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-070117-3';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EFG-110824-3' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-130816-2' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-080201-1';

update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-111118-1' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-130816-2' and mrel_mrkr_2_zdb_id='ZDB-EFG-070117-3';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-110824-3' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-130816-2' and mrel_mrkr_2_zdb_id='ZDB-EFG-080201-1';


insert into record_attribution(recattrib_data_Zdb_id,recattrib_source_zdb_id) select aliasid,'ZDB-PUB-130709-52' from tmp_dalias;
drop table tmp_dalias;








