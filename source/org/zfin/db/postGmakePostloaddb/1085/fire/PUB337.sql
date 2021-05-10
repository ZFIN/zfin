--liquibase formatted sql
--changeset xiang:PUB337

create temp table tmp_id (id varchar(50))
with no log;

insert into tmp_id
  select get_id('JRNL')
    from single;

insert into zdb_active_source
  select id from tmp_id;

insert into journal (jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_print_issn, jrnl_is_nice)
 select id, 'Journal of Oral Science Research', 'J Oral Sci Res', '1671-7651', 'f'
   from tmp_id;

delete from tmp_id;

insert into tmp_id
  select get_id('JRNL')
    from single;

insert into zdb_active_source
  select id from tmp_id;

insert into journal (jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_print_issn, jrnl_is_nice)                  
 select id, 'The Journal of Fudan University (Natural Science)', 'Fudan Univ J Nat Sci', '0427-7104', 'f'
   from tmp_id;              

delete from tmp_id;

insert into tmp_id
  select get_id('JRNL')
    from single;

insert into zdb_active_source
  select id from tmp_id;

insert into journal (jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_print_issn, jrnl_online_issn, jrnl_is_nice, 
                     jrnl_medabbrev, jrnl_isoabbrev, jrnl_nlmid)
 select id, 'Biorheology', 'Biorheology', '0006-355X', '1878-5034', 'f',
            'Biorheology', 'Biorheology', '0372526' 
   from tmp_id;

delete from tmp_id;

insert into tmp_id
  select get_id('JRNL')
    from single;

insert into zdb_active_source
  select id from tmp_id;

insert into journal (jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_print_issn, jrnl_online_issn, jrnl_is_nice,
                     jrnl_medabbrev, jrnl_isoabbrev, jrnl_nlmid)
 select id, 'Journal of liquid chromatography & related technologies', 'J Liq Chromatogr Relat Technol', '1082-6076', '1520-572X', 'f',
            'J Liq Chromatogr Relat Technol', 'J. Liq. Chromatogr. Relat. Technol.', '9605507'
   from tmp_id;

