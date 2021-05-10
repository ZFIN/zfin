--liquibase formatted sql
--changeset pm:DLOAD-865

update construct_component set cc_order=22 where cc_construct_zdb_id='ZDB-TGCONSTRCT-141113-2' and cc_order=20;
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-141113-2','text component','coding sequence component','-',1,20);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-141113-2','coding sequence of ','coding sequence component','ZDB-EFG-181218-3','oPRE',1,21);

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-141113-2','ZDB-TGCONSTRCT-141113-2','ZDB-EFG-181218-3','coding sequence of');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-141113-2','ZDB-TGCONSTRCT-141113-2','ZDB-EFG-181218-3','coding sequence of');
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
drop table tmp_mrel;


insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-141113-4','text component','coding sequence component','-',1,12);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-141113-4','coding sequence of ','coding sequence component','ZDB-EFG-181218-3','oPRE',1,13);

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-141113-4','ZDB-TGCONSTRCT-141113-4','ZDB-EFG-181218-3','coding sequence of');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-141113-4','ZDB-TGCONSTRCT-141113-4','ZDB-EFG-181218-3','coding sequence of');
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
drop table tmp_mrel;

update construct set construct_name='Tg(UAS-ADV.E1b:GCaMP6s-2A-mCherry-2A-LY-CFP-oPRE)' where construct_zdb_id='ZDB-TGCONSTRCT-141113-2';
update marker set mrkr_name='Tg(UAS-ADV.E1b:GCaMP6s-2A-mCherry-2A-LY-CFP-oPRE)' where mrkr_zdb_id='ZDB-TGCONSTRCT-141113-2';
update marker set mrkr_abbrev='Tg(UAS-ADV.E1b:GCaMP6s-2A-mCherry-2A-LY-CFP-oPRE)' where mrkr_zdb_id='ZDB-TGCONSTRCT-141113-2';

update construct set construct_name='Tg(14xUAS-E1B:Ocu.Hbb2-NTR-TagRFPT-oPRE)' where construct_zdb_id='ZDB-TGCONSTRCT-141113-4';
update marker set mrkr_name='Tg(14xUAS-E1B:Ocu.Hbb2-NTR-TagRFPT-oPRE)' where mrkr_zdb_id='ZDB-TGCONSTRCT-141113-4';
update marker set mrkr_abbrev='Tg(14xUAS-E1B:Ocu.Hbb2-NTR-TagRFPT-oPRE)' where mrkr_zdb_id='ZDB-TGCONSTRCT-141113-4';








