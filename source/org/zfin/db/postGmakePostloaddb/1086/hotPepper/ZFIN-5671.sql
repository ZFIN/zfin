--liquibase formatted sql
--changeset prita:ZFIN-5671

update construct set construct_name='Tg(rnu6-32:CRISPR1-tyr,rnu6-32:CRISPR1-insra,rnu6-14:CRISPR2-insra,rnu6-7:CRISPR1-insrb,rnu6-279:CRISPR2-insrb,cryaa:Cerulean)' where construct_zdb_id='ZDB-TGCONSTRCT-170419-4';
update marker set mrkr_name='Tg(rnu6-32:CRISPR1-tyr,rnu6-32:CRISPR1-insra,rnu6-14:CRISPR2-insra,rnu6-7:CRISPR1-insrb,rnu6-279:CRISPR2-insrb,cryaa:Cerulean)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170419-4';
update marker set mrkr_abbrev='Tg(rnu6-32:CRISPR1-tyr,rnu6-32:CRISPR1-insra,rnu6-14:CRISPR2-insra,rnu6-7:CRISPR1-insrb,rnu6-279:CRISPR2-insrb,cryaa:Cerulean)' where mrkr_zdb_id='ZDB-TGCONSTRCT-170419-4';

delete from construct_component where cc_construct_Zdb_id='ZDB-TGCONSTRCT-170419-4' and cc_order=14;

insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','controlled vocab component','promoter component','ZDB-CV-150506-11',',',4,14);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','promoter of','promoter component','ZDB-NCRNAG-170419-3','rnu6-7',4,15);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','controlled vocab component','promoter component','ZDB-CV-150506-10',':',4,16);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','coding sequence of','coding component','ZDB-CRISPR-150924-3','CRISPR1-insrb',4,17);

insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','controlled vocab component','promoter component','ZDB-CV-150506-11',',',5,18);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','promoter of','promoter component','ZDB-NCRNAG-170419-4','rnu6-279',5,19);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','controlled vocab component','promoter component','ZDB-CV-150506-10',':',5,20);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','coding sequence of','coding component','ZDB-CRISPR-150925-1','CRISPR2-insrb',5,21);

insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','controlled vocab component','promoter component','ZDB-CV-150506-11',',',6,22);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','promoter of','promoter component','ZDB-GENE-020508-1','cryaa',6,23);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','controlled vocab component','promoter component','ZDB-CV-150506-10',':',6,24);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','coding sequence of','coding component','ZDB-EFG-110824-3','Cerulean',6,25);

insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-170419-4','controlled vocab component','construct wrapper component','ZDB-CV-150506-8',')',6,26);

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50)) with no log;

insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-NCRNAG-170419-3','promoter of');
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-CRISPR-150924-3','coding sequence of');
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-NCRNAG-170419-4','promoter of');
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-CRISPR-150925-1','coding sequence of');
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-GENE-020508-1','promoter of');
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-EFG-110824-3','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;

drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50)) with no log;

insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-NCRNAG-170419-3','promoter of');
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-CRISPR-150924-3','coding sequence of');
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-NCRNAG-170419-4','promoter of');
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-CRISPR-150925-1','coding sequence of');
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-GENE-020508-1','promoter of');
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-170419-4','ZDB-EFG-110824-3','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;

insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-150410-5','standard' from tmp_mrel;

drop table tmp_mrel;