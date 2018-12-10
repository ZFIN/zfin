--liquibase formatted sql
--changeset pm:CUR-861
\copy (select distinct cc_construct_zdb_id,cc_construct_zdb_id,'ZDB-EREGION-181119-1','promoter of' from construct_component where(cc_component like '%E1b%' or cc_component='ADV.')) to 'changeconstructs.csv' delimiter ',';
update construct_component set cc_component_zdb_id='ZDB-EREGION-181119-1' where cc_component='E1b';

update construct_component set cc_component_zdb_id='ZDB-EREGION-181119-1' where cc_component='ADV.E1b';
delete from construct_component where cc_component='ADV.';

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
\copy tmp_cmrel from 'changeconstructs.csv' delimiter ',';
delete from tmp_cmrel where consid in (select conmrkrrel_construct_zdb_id from construct_marker_relationship where conmrkrrel_mrkr_zdb_id='ZDB-EREGION-181119-1');
update tmp_cmrel set relid=get_id('CMREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;

create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
\copy tmp_mrel from 'changeconstructs.csv' delimiter ',';
update tmp_mrel set relid=get_id('MREL');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;

drop table tmp_mrel;







