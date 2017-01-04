--liquibase formatted sql
--changeset xiang:14602

create temp table tmp_id (id varchar(50))
with no log;

insert into tmp_id
  select get_id('DALIAS')
    from single;

insert into zdb_active_data  
  select id from tmp_id;

insert into data_alias (dalias_zdb_id,
			dalias_data_zdb_id,
			dalias_alias,
			dalias_group_id)
 select id, 'ZDB-GENO-160927-1', 'crystal', 1
   from tmp_id;

insert into record_attribution
  (recattrib_data_zdb_id, recattrib_source_zdb_id)
 select id, "ZDB-PUB-160707-3"
  from tmp_id
 ;

delete from tmp_id;

insert into tmp_id
  select get_id('DALIAS')
    from single;

insert into zdb_active_data
  select id from tmp_id;

insert into data_alias (dalias_zdb_id,
			dalias_data_zdb_id,
			dalias_alias,
			dalias_group_id)
 select id, 'ZDB-GENO-130807-6', 'sheer', 1
   from tmp_id;

insert into record_attribution
  (recattrib_data_zdb_id, recattrib_source_zdb_id)
 select id, "ZDB-PUB-160331-1"
  from tmp_id
 ;

