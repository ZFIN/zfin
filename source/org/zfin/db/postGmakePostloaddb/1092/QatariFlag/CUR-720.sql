--liquibase formatted sql
--changeset pm:CUR-720

update construct_component set cc_order=9 where cc_order=7 and cc_construct_zdb_id='ZDB-TGCONSTRCT-150810-1';
update construct_component set cc_order=10 where cc_order=8 and cc_construct_zdb_id='ZDB-TGCONSTRCT-150810-1';
update construct_component set cc_order=11 where cc_component='nanos3' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150810-1';
update construct_component set cc_order=12 where cc_component=')' and cc_construct_zdb_id='ZDB-TGCONSTRCT-150810-1';

insert into construct_component (cc_construct_Zdb_id,cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-150810-1','coding sequence of','coding component','ZDB-EREGION-110822-3','FTASE',1,7);
insert into construct_component (cc_construct_Zdb_id,cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-150810-1','text component','coding component','ZDB-EREGION-110822-3','-',1,8);
update construct set construct_name='Tg(kop:mCherry-FTASE-UTR-nanos3)' where construct_Zdb_id='ZDB-TGCONSTRCT-150810-1';

update marker set mrkr_name='Tg(kop:mCherry-FTASE-UTR-nanos3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-150810-1';


update marker set mrkr_abbrev='Tg(kop:mCherry-FTASE-UTR-nanos3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-150810-1';


create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));


insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-150810-1','ZDB-EREGION-110822-3','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;

drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));


insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-150810-1','ZDB-EREGION-110822-3','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;

insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-150416-4','standard' from tmp_mrel;

drop table tmp_mrel;


update construct_component set cc_order=9 where cc_order=7 and cc_construct_zdb_id='ZDB-TGCONSTRCT-070406-1';
update construct_component set cc_order=10 where cc_order=8 and cc_construct_zdb_id='ZDB-TGCONSTRCT-070406-1';
update construct_component set cc_order=11 where cc_component='nanos3' and cc_construct_zdb_id='ZDB-TGCONSTRCT-070406-1';
update construct_component set cc_order=12 where cc_component=')'  and cc_construct_zdb_id='ZDB-TGCONSTRCT-070406-1';

insert into construct_component (cc_construct_Zdb_id,cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-070406-1','coding sequence of','coding component','ZDB-EREGION-110822-3','FTASE',1,7);
insert into construct_component (cc_construct_Zdb_id,cc_component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-070406-1','text component','coding component','ZDB-EREGION-110822-3','-',1,8);

update construct set construct_name='Tg(kop:EGFP-FTASE-UTR-nanos3)' where construct_Zdb_id='ZDB-TGCONSTRCT-070406-1';

update marker set mrkr_name='Tg(kop:EGFP-FTASE-UTR-nanos3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-070406-1';


update marker set mrkr_abbrev='Tg(kop:EGFP-FTASE-UTR-nanos3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-070406-1';


create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));


insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-070406-1','ZDB-EREGION-110822-3','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;

drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));


insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-070406-1','ZDB-EREGION-110822-3','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;

insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-050907-5','standard' from tmp_mrel;

drop table tmp_mrel;

update construct set construct_name='Tg(kop:EGFP-LIFEACT-UTR-nanos3)' where construct_Zdb_id='ZDB-TGCONSTRCT-141104-1';

update marker set mrkr_name='Tg(kop:EGFP-LIFEACT-UTR-nanos3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-141104-1';


update marker set mrkr_abbrev='Tg(kop:EGFP-LIFEACT-UTR-nanos3)' where mrkr_zdb_id='ZDB-TGCONSTRCT-141104-1';


create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));


insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-141104-1','ZDB-EREGION-110816-13','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;

drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));


insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-141104-1','ZDB-EREGION-110816-13','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;

insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-140723-6','standard' from tmp_mrel;

drop table tmp_mrel;


