--liquibase formatted sql
--changeset prita:ZFIN-5733

update construct set construct_name='Tg(5xUAS:FMAVenus-2A-3xNLS-HA-ECFP)' where construct_zdb_id='ZDB-TGCONSTRCT-151204-8';
update marker set mrkr_name='Tg(5xUAS:FMAVenus-2A-3xNLS-HA-ECFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-151204-8';
update marker set mrkr_abbrev='Tg(5xUAS:FMAVenus-2A-3xNLS-HA-ECFP)' where mrkr_zdb_id='ZDB-TGCONSTRCT-151204-8';

update  construct_component set cc_order=15 where cc_construct_Zdb_id='ZDB-TGCONSTRCT-151204-8' and cc_component='EGFP';
update  construct_component set cc_order=16 where cc_construct_Zdb_id='ZDB-TGCONSTRCT-151204-8' and cc_component=')';
update  construct_component set cc_component_zdb_id='ZDB-EFG-111115-1' where cc_construct_Zdb_id='ZDB-TGCONSTRCT-151204-8' and cc_component='EGFP';
update  construct_component set cc_component='ECFP' where cc_construct_Zdb_id='ZDB-TGCONSTRCT-151204-8' and cc_component='EGFP';

insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component_zdb_id,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-151204-8','coding sequence of','coding component','ZDB-EREGION-110822-1','HA',1,13);
insert into construct_component (cc_construct_Zdb_id,cc_Component_type,cc_component_category,cc_component,cc_cassette_number,cc_order) values ('ZDB-TGCONSTRCT-151204-8','text component','coding component','-',1,14);

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50)) with no log;

insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-151204-8','ZDB-EREGION-110822-1','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-EFG-111115-1' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-151204-8' and conmrkrrel_mrkr_zdb_id='ZDB-EFG-070117-1';

drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50)) with no log;

insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-151204-8','ZDB-EREGION-110822-1','coding sequence of');



insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;

insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-EFG-111115-1' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-151204-8' and mrel_mrkr_2_zdb_id='ZDB-EFG-070117-1';

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-140710-12','standard' from tmp_mrel;

drop table tmp_mrel;