--liquibase formatted sql
--changeset prita:ZFIN-5966


update construct set construct_name='Tg(hsp70l:npvf-RFRP1)' where construct_zdb_id='ZDB-TGCONSTRCT-171004-1';
update marker set mrkr_name='Tg(hsp70l:npvf-RFRP1)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-1';
update marker set mrkr_abbrev='Tg(hsp70l:npvf-RFRP1)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-1';

update construct set construct_name='Tg(hsp70l:npvf-RFRP2)' where construct_zdb_id='ZDB-TGCONSTRCT-171004-2';
update marker set mrkr_name='Tg(hsp70l:npvf-RFRP2)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-2';
update marker set mrkr_abbrev='Tg(hsp70l:npvf-RFRP2)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-2';

update construct set construct_name='Tg(hsp70l:npvf-RFRP3)' where construct_zdb_id='ZDB-TGCONSTRCT-171004-3';
update marker set mrkr_name='Tg(hsp70l:npvf-RFRP3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-3';
update marker set mrkr_abbrev='Tg(hsp70l:npvf-RFRP3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-3';


update construct set construct_name='Tg(hsp70l:npvf-RFRP1,2)' where construct_zdb_id='ZDB-TGCONSTRCT-171004-4';
update marker set mrkr_name='Tg(hsp70l:npvf-RFRP1,2)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-4';
update marker set mrkr_abbrev='Tg(hsp70l:npvf-RFRP1,2)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-4';

update construct set construct_name='Tg(hsp70l:npvf-RFRP1,3)' where construct_zdb_id='ZDB-TGCONSTRCT-171004-5';
update marker set mrkr_name='Tg(hsp70l:npvf-RFRP1,3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-5';
update marker set mrkr_abbrev='Tg(hsp70l:npvf-RFRP1,3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-5';

update construct set construct_name='Tg(hsp70l:npvf-RFRP2,3)' where construct_zdb_id='ZDB-TGCONSTRCT-171004-6';
update marker set mrkr_name='Tg(hsp70l:npvf-RFRP2,3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-6';
update marker set mrkr_abbrev='Tg(hsp70l:npvf-RFRP2,3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-171004-6';




create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-171004-1','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;
create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-171004-1','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-171107-11','standard' from tmp_mrel;
drop table tmp_mrel;

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-171004-2','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;
create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-171004-2','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-171107-11','standard' from tmp_mrel;
drop table tmp_mrel;

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-171004-3','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;
create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-171004-3','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-171107-11','standard' from tmp_mrel;
drop table tmp_mrel;

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-171004-4','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;
create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-171004-4','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-171107-11','standard' from tmp_mrel;
drop table tmp_mrel;

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-171004-5','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;
create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50)) ;
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-171004-5','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-171107-11','standard' from tmp_mrel;
drop table tmp_mrel;

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-171004-6','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;
create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-171004-6','ZDB-GENE-070424-226','coding sequence of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-171107-11','standard' from tmp_mrel;
drop table tmp_mrel;


