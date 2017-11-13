--liquibase formatted sql
--changeset pm:ZFIN3981

create temp table tmp_fishattrib (fishid varchar(50), genoid varchar(50),source varchar(50));

insert into tmp_fishattrib select distinct fish_zdb_id,fish_genotype_zdb_id ,recattrib_source_zdb_id
from  fish,record_attribution
where fish_Zdb_id=recattrib_Data_zdb_id;



insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id)
select distinct genoid,source from tmp_fishattrib
where not exists (select 1 from record_attribution where recattrib_Data_zdb_id=genoid and recattrib_source_zdb_id=source);


insert into record_attribution (recattrib_Data_zdb_id,recattrib_source_zdb_id)
select distinct genofeat_feature_zdb_id,source from tmp_fishattrib,genotype_feature
where genoid=genofeat_geno_zdb_id
and not exists (select 1 from record_attribution where recattrib_Data_zdb_id=genofeat_feature_zdb_id and  recattrib_source_zdb_id=source);



drop table tmp_fishattrib;
