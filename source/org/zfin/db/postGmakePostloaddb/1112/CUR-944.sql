--liquibase formatted sql
--changeset pm:CUR-944

update construct_component set cc_order=15 where cc_construct_zdb_id='ZDB-TGCONSTRCT-191125-4' and cc_order=11 and cc_component=')';

insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_component_zdb_id,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-191125-4','controlled vocab component','cassette delimiter',',','ZDB-CV-150506-11',2,11);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-191125-4','promoter of ','promoter component','ZDB-GENE-020508-1','cryaa',2,12);
 insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-191125-4','controlled vocab component ','promoter component','ZDB-CV-150506-10',':',2,13);
  insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-191125-4','coding sequence of ','coding sequence component','ZDB-EFG-070117-1','EGFP',2,14);



create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-191125-4','ZDB-TGCONSTRCT-191125-4','ZDB-EFG-070117-1','coding sequence of');
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-191125-4','ZDB-TGCONSTRCT-191125-4','ZDB-GENE-020508-1','promoter of');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-191125-4','ZDB-TGCONSTRCT-191125-4','ZDB-EFG-070117-1','coding sequence of');
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-191125-4','ZDB-TGCONSTRCT-191125-4','ZDB-GENE-020508-1','promoter of');
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
drop table tmp_mrel;




update construct set construct_name='Tg(Mmu.Smarcd3-F6-E1B:Cre-ERT2,cryaa:EGFP)' where construct_zdb_id='ZDB-TGCONSTRCT-191125-4';
update marker set mrkr_name='Tg(Mmu.Smarcd3-F6-E1B:Cre-ERT2,cryaa:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-191125-4';
update marker set mrkr_abbrev='Tg(Mmu.Smarcd3-F6-E1B:Cre-ERT2,cryaa:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-191125-4';


--next construct

update construct_component set cc_order=15 where cc_construct_zdb_id='ZDB-TGCONSTRCT-191125-5' and cc_order=11 and cc_component=')';

insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_component_zdb_id,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-191125-5','controlled vocab component','cassette delimiter',',','ZDB-CV-150506-11',2,11);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-191125-5','promoter of ','promoter component','ZDB-GENE-020508-1','cryaa',2,12);
 insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-191125-5','controlled vocab component ','promoter component','ZDB-CV-150506-10',':',2,13);
  insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order)
 values ('ZDB-TGCONSTRCT-191125-5','coding sequence of ','coding sequence component','ZDB-EFG-070117-1','EGFP',2,14);



create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-191125-5','ZDB-TGCONSTRCT-191125-5','ZDB-EFG-070117-1','coding sequence of');
insert into tmp_cmrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-191125-5','ZDB-TGCONSTRCT-191125-5','ZDB-GENE-020508-1','promoter of');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-191125-5','ZDB-TGCONSTRCT-191125-5','ZDB-EFG-070117-1','coding sequence of');
insert into tmp_mrel (relid, consid ,mrkrid,reltype) values ('ZDB-TGCONSTRCT-191125-5','ZDB-TGCONSTRCT-191125-5','ZDB-GENE-020508-1','promoter of');
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
drop table tmp_mrel;




update construct set construct_name='Tg(Mmu.Smarcd3-F6-gata2a:Cre-ERT2,cryaa:EGFP)' where construct_zdb_id='ZDB-TGCONSTRCT-191125-5';
update marker set mrkr_name='Tg(Mmu.Smarcd3-F6-gata2a:Cre-ERT2,cryaa:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-191125-5';
update marker set mrkr_abbrev='Tg(Mmu.Smarcd3-F6-gata2a:Cre-ERT2,cryaa:EGFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-191125-5';










