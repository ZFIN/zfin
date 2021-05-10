--liquibase formatted sql
--changeset pm:DLOAD-598
--adding relationships to REX2 for ZDB-ETCONSTRCT-130214-1 and ZDB-ETCONSTRCT-130214-2


 update construct set construct_name='Et(REX2-Mmu.Fos:GAL4FF)' where construct_zdb_id='ZDB-ETCONSTRCT-130214-1';
 update marker set mrkr_name='Et(REX2-Mmu.Fos:GAL4FF)' where mrkr_zdb_id='ZDB-ETCONSTRCT-130214-1';
 update marker set mrkr_abbrev='Et(REX2-Mmu.Fos:GAL4FF)' where mrkr_zdb_id='ZDB-ETCONSTRCT-130214-1';

  update construct set construct_name='Et(REX2-SCP1:GAL4FF)' where construct_zdb_id='ZDB-ETCONSTRCT-130214-2';
 update marker set mrkr_name='Et(REX2-SCP1:GAL4FF)' where mrkr_zdb_id='ZDB-ETCONSTRCT-130214-2';
 update marker set mrkr_abbrev='Et(REX2-SCP1:GAL4FF)' where mrkr_zdb_id='ZDB-ETCONSTRCT-130214-2';


create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-ETCONSTRCT-130214-1','ZDB-EREGION-181127-1','promoter of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;
create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-ETCONSTRCT-130214-1','ZDB-EREGION-181127-1','promoter of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-130110-30','standard' from tmp_mrel;
drop table tmp_mrel;

create temp table tmp_cmrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_cmrel(relid,consid,mrkrid,reltype) values (get_id('CMREL'),'ZDB-ETCONSTRCT-130214-2','ZDB-EREGION-181127-1','promoter of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_cmrel;
insert into construct_marker_relationship(conmrkrrel_zdb_id,conmrkrrel_construct_zdb_id,conmrkrrel_mrkr_zdb_id,conmrkrrel_relationship_type) select relid,consid,mrkrid,reltype from tmp_cmrel;
drop table tmp_cmrel;
create temp table tmp_mrel (relid varchar(50), consid varchar(50),mrkrid varchar(50),reltype varchar(50));
insert into tmp_mrel(relid,consid,mrkrid,reltype) values (get_id('MREL'),'ZDB-ETCONSTRCT-130214-2','ZDB-EREGION-181127-1','promoter of');
insert into zdb_active_data (zactvd_zdb_id) select relid from tmp_mrel;
insert into marker_relationship(mrel_zdb_id,mrel_mrkr_1_zdb_id,mrel_mrkr_2_zdb_id,mrel_type) select relid,consid,mrkrid,reltype from tmp_mrel;
insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id,recattrib_source_type) select relid,'ZDB-PUB-130110-30','standard' from tmp_mrel;
drop table tmp_mrel;





