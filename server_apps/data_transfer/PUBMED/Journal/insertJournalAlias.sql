begin work;

create temp table temp_source_alias
  (
    t_salias_source_zdb_id text,
    t_salias_alias text
  ) ;

copy temp_source_alias from '<!--|TARGETROOT|-->/server_apps/data_transfer/PUBMED/Journal/aliasList' (delimiter '|');

create temp table tmp_ids as select get_id('SALIAS') as id, t_salias_source_zdb_id, t_salias_alias
 from temp_source_alias;


insert into zdb_active_source
  select id from tmp_ids;

insert into source_alias (salias_zdb_id, salias_source_zdb_id, salias_alias)
  select id, t_salias_source_zdb_id, t_salias_alias
  from tmp_ids;

commit work;
--rollback work;

