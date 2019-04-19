--liquibase formatted sql
--changeset pm:CUR-902


create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select 'ZDB-TGCONSTRCT-180129-4', 'ZDB-TGCONSTRCT-180129-4', construct_name  from  construct where construct_zdb_id='ZDB-TGCONSTRCT-180129-4';
insert into tmp_dalias (aliasid, consid ,consname) values('ZDB-TGCONSTRCT-180129-4', 'ZDB-TGCONSTRCT-180129-4', 'Tg(myo6b:RiboTag)');


update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;

update construct set construct_name='Tg(myo6b:GFP-p2A-rpl10a-3xHA)' where construct_zdb_id='ZDB-TGCONSTRCT-180129-4';
update marker set mrkr_name='Tg(myo6b:GFP-p2A-rpl10a-3xHA)' where mrkr_zdb_id='ZDB-TGCONSTRCT-180129-4';
update marker set mrkr_abbrev ='Tg(myo6b:GFP-p2A-rpl10a-3xHA)' where mrkr_zdb_id='ZDB-TGCONSTRCT-180129-4';


update construct_component set cc_component_zdb_id='ZDB-EFG-070117-2'  where cc_component='HA' and cc_construct_zdb_id='ZDB-TGCONSTRCT-180129-4';
update construct_component set cc_component='GFP'  where cc_component='HA' and cc_construct_zdb_id='ZDB-TGCONSTRCT-180129-4';
delete from  construct_component  where cc_order=7 and cc_construct_zdb_id='ZDB-TGCONSTRCT-180129-4';
delete from  construct_component  where cc_order=8 and cc_construct_zdb_id='ZDB-TGCONSTRCT-180129-4';
delete from  construct_component  where cc_order=12 and cc_construct_zdb_id='ZDB-TGCONSTRCT-180129-4';

insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-180129-4','coding sequence of ','coding sequence component','ZDB-GENE-030131-2025','rpl10a',1,12);


update construct_component set cc_order=16 where cc_order=13 and cc_construct_zdb_id='ZDB-TGCONSTRCT-180129-4';
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-180129-4','text component ','coding sequence component','-',1,13);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-180129-4','text component ','coding sequence component','3x',1,14);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-180129-4','coding sequence of ','coding sequence component','ZDB-EREGION-110822-1','HA',1,15);


create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-180129-4','ZDB-TGCONSTRCT-180129-4','ZDB-GENE-030131-2025','coding sequence of');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-180129-4','ZDB-TGCONSTRCT-180129-4','ZDB-GENE-030131-2025','coding sequence of');
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
drop table tmp_mrel;


insert into record_attribution(recattrib_data_Zdb_id,recattrib_source_zdb_id) select aliasid,'ZDB-PUB-190102-5' from tmp_dalias;
drop table tmp_dalias;








