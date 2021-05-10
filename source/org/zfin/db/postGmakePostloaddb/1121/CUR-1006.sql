--liquibase formatted sql
--changeset pm:CUR-1006





update construct_component set cc_component_zdb_id='ZDB-ENHANCER-201208-1' where cc_component_zdb_id='ZDB-EREGION-160620-1';
update construct_marker_relationship set conmrkrrel_mrkr_zdb_id='ZDB-ENHANCER-201208-1' where conmrkrrel_mrkr_zdb_id='ZDB-EREGION-160620-1';
update marker_relationship set mrel_mrkr_2_zdb_id='ZDB-ENHANCER-201208-1' where mrel_mrkr_2_zdb_id='ZDB-EREGION-160620-1';


create temp table tmp_dalias (aliasid varchar(50), consid varchar(50),consname text);
insert into tmp_dalias (aliasid, consid ,consname) select construct_zdb_id , construct_zdb_id, construct_name  from construct where construct_name like '%LLE%';
insert into tmp_dalias (aliasid, consid ,consname) values( 'ZDB-TGCONSTRCT-160613-1' , 'ZDB-TGCONSTRCT-160613-1', 'Tg(unc45b:TFP)') ;

update tmp_dalias set aliasid=get_id('DALIAS');
insert into zdb_active_data (zactvd_zdb_id) select aliasid from tmp_dalias;
insert into data_alias(dalias_zdb_id,dalias_data_zdb_id,dalias_alias,dalias_group_id) select aliasid,consid,consname,1 from tmp_dalias;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type) select aliasid,'ZDB-PUB-160407-4', 'standard' from tmp_dalias where consname like '%LLE%';
insert into zdb_replaced_data(zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-EREGION-160620-1','ZDB-ENHANCER-201208-1');
insert into withdrawn_data(wd_old_zdb_id,wd_new_zdb_id) values ('ZDB-EREGION-160620-1','ZDB-ENHANCER-201208-1');
delete from zdb_active_data where zactvd_zdb_id='ZDB-EREGION-160620-1';
drop table tmp_dalias;














