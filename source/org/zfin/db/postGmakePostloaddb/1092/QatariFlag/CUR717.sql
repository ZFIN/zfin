--liquibase formatted sql
--changeset pm:CUR717

create temp table ftrattrib (ftr varchar(50), source varchar(50));
insert into ftrattrib (ftr,source) select distinct recattrib_data_zdb_id, recattrib_Source_zdb_id from record_attribution, feature_marker_relationship
where fmrel_ftr_zdb_id=recattrib_Data_zdb_id and fmrel_type like '%sequence feature%' and fmrel_mrkr_zdb_id like '%CONSTRCT%';

create temp table constrattrib (constr varchar(50), sourceid varchar(50));
insert into constrattrib (constr,sourceid) select distinct fmrel_mrkr_zdb_id,source from ftrattrib,feature_marker_relationship
where fmrel_ftr_zdb_id=ftr and fmrel_mrkr_zdb_id like '%CONSTRCT%' and not exists (select 1 from record_attribution where recattrib_data_zdb_id=fmrel_mrkr_zdb_id and recattrib_source_zdb_id=source);


create temp table codingattrib (mrkr varchar(50), sourceid2 varchar(50));
insert into codingattrib (mrkr,sourceid2) select distinct mrel_mrkr_2_zdb_id,sourceid from constrattrib,marker_relationship
where mrel_mrkr_1_zdb_id=constr and mrel_type like '%coding sequence%' and not exists (select 1 from record_attribution where recattrib_data_zdb_id=mrel_mrkr_2_zdb_id and recattrib_source_zdb_id=sourceid);


insert into record_attribution (recattrib_Data_zdb_id, recattrib_source_zdb_id) select * from constrattrib;
insert into record_attribution (recattrib_Data_zdb_id, recattrib_source_zdb_id) select * from codingattrib;



