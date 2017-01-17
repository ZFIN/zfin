--liquibase formatted sql
--changeset xiang:14850

create temp table tmp_id (id varchar(50))
with no log;

insert into tmp_id
  select get_id('JRNL')
    from single;

insert into zdb_active_source
  select id from tmp_id;

insert into journal (jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_print_issn)
 select id, 'African Journal of Biotechnology', 'Afr. J. Biotechnol.', '1684-5315'
   from tmp_id;

