begin work;

create temp table temp_source_alias
  (
    t_salias_source_zdb_id varchar(50),
    t_salias_alias varchar(255)
  ) ;

\copy temp_source_alias  from '<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/Journal/aliasList' delimiter ',';
  insert into temp_source_alias;

create temp table tmp_ids as select get_id('SALIAS') as id, t_salias_source_zdb_id, t_salias_alias
 from temp_source_alias;


insert into zdb_active_source
  select id from tmp_ids;

insert into source_alias (salias_zdb_id, salias_source_zdb_id, salias_alias)
  select id, t_salias_source_zdb_id, t_salias_alias
  from tmp_ids;



--rollback work;

