--liquibase formatted sql
--changeset prita:INF-3157

create temp table tmp_ftratt (ftrzdb varchar(50) , pub varchar(50));
insert into tmp_ftratt  select distinct feature_zdb_id,recattrib_source_zdb_id
from feature, feature_marker_relationship,record_attribution
where fmrel_ftr_zdb_id=feature_zdb_id  and fmrel_ftr_zdb_id=recattrib_data_zdb_id
and fmrel_zdb_id not in (select recattrib_data_zdb_id from record_attribution where recattrib_data_zdb_id like '%FMREL%')
order by feature_zdb_id,recattrib_source_zdb_id;

create temp table tmp_featureatt (ftzdb varchar(50));
insert into tmp_featureatt  select ftrzdb from tmp_ftratt  group by ftrzdb having count(pub) = 1;

insert into record_attribution (recattrib_Source_zdb_id, recattrib_Data_zdb_id)
select distinct pub,fmrel_zdb_id
from tmp_featureatt, feature_marker_relationship,tmp_ftratt
where ftzdb=fmrel_ftr_zdb_id
and ftzdb=ftrzdb
and fmrel_zdb_id not in (Select recattrib_data_zdb_id from record_attribution);

drop table tmp_ftratt;
drop table tmp_featureatt;

