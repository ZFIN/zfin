--liquibase formatted sql
--changeset pm:CUR-751

update construct_Component set cc_component_zdb_id='ZDB-GENE-050320-24' where cc_construct_Zdb_id='ZDB-TGCONSTRCT-151007-29' and cc_order=11;
update construct_Component set cc_component_zdb_id='ZDB-GENE-050320-24' where cc_construct_Zdb_id='ZDB-TGCONSTRCT-170914-2' and cc_order=13;
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-GENE-050320-24' where conmrkrrel_construct_zdb_id='ZDB-TGCONSTRCT-151007-29' and conmrkrrel_mrkr_zdb_id='ZDB-EREGION-110816-1';

update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-GENE-050320-24' where mrel_mrkr_1_zdb_id='ZDB-TGCONSTRCT-151007-29' and mrel_mrkr_2_zdb_id='ZDB-EREGION-110816-1';

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));


insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-TGCONSTRCT-170914-2','ZDB-GENE-050320-24','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;

insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;

drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));


insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-TGCONSTRCT-170914-2','ZDB-GENE-050320-24','coding sequence of');

insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;

insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;

insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-170618-19','standard' from tmp_mrel;

drop table tmp_mrel;

