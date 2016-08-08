begin work;

create temp table tmp_new_journals 
  (
    t_jrnl_zdb_id varchar(50),
    t_jrnl_name varchar(255),
    t_jrnl_abbrev varchar(255),
    t_jrnl_print_issn varchar(100),
    t_jrnl_online_issn varchar(100),
    t_jrnl_nlmid varchar(100),
    t_jrnl_medabbrev varchar(255),
    t_jrnl_isoabbrev varchar(255)
  ) with no log;

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"Gene Structure and Expression", "Gene Structure and Expression", "0167-4781");

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"General Subjects", "General Subjects", "0304-4165");

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"Protein Structure and Molecular Enzymology", "Protein Structure and Molecular Enzymology", "0167-4838");

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"Biomembranes", "Biomembranes", "0067-8864");

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"Reviews on Cancer", "Reviews on Cancer", "0304-419X");

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"Molecular Basis of Disease", "Molecular Basis of Disease", "0925-4439");

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"Molecular Cell Research", "Molecular Cell Research", "0167-4889");

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"Molecular and Cell Biology of Lipids", "Molecular and Cell Biology of Lipids", "1388-1981");

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"Bioenergetics", "Bioenergetics", "0005-2728");

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"Gene Regulatory Mechanisms", "Gene Regulatory Mechanisms", "1874-9399");

insert into tmp_new_journals(t_jrnl_zdb_id, t_jrnl_name, t_jrnl_abbrev, t_jrnl_print_issn)
  values(get_id("JRNL"),"Proteins and Proteomics", "Proteins and Proteomics", "1570-9639");

insert into zdb_active_source
  select t_jrnl_zdb_id 
   from tmp_new_journals;

insert into journal (jrnl_zdb_id, jrnl_name, jrnl_abbrev, jrnl_print_issn, jrnl_online_issn, jrnl_nlmid, jrnl_medabbrev, jrnl_isoabbrev, jrnl_publisher, jrnl_is_nice)
  select t_jrnl_zdb_id, "BBA "||t_jrnl_name, "BBA "||t_jrnl_abbrev, t_jrnl_print_issn, t_jrnl_online_issn, t_jrnl_nlmid, t_jrnl_medabbrev, t_jrnl_isoabbrev, "Elsevier", 'f'
  from tmp_new_journals;

create table publication_journals
  (
    pub_id varchar(50),
    pub_vol varchar(10),
    pub_year varchar(10),
    journal_title varchar(255),
    journal_issn varchar(100)
  ) in tbldbs1;

load from toSplit.csv
  insert into publication_journals;

alter table publication_journals add journal_id varchar(50);

select * from tmp_new_journals;

update publication_journals
   set journal_id = (select t_jrnl_zdb_id
                       from tmp_new_journals
                      where t_jrnl_name = journal_title);

select distinct journal_id from publication_journals;

select * from publication_journals where journal_id is null;

update publication
set pub_jrnl_zdb_id = (select journal_id 
                         from publication_journals
                        where journal_id is not null and zdb_id = pub_id)
 where exists(select "x" from publication_journals where pub_id = zdb_id);

drop table publication_journals;

select count(*) from publication where pub_jrnl_zdb_id = "ZDB-JRNL-050621-330";

delete from zdb_active_source where zactvs_zdb_id = "ZDB-JRNL-050621-330";

select count(*) from publication where pub_jrnl_zdb_id = "ZDB-JRNL-060210-1";

delete from zdb_active_source where zactvs_zdb_id = "ZDB-JRNL-060210-1";

select count(*) from publication where pub_jrnl_zdb_id = "ZDB-JRNL-051107-3";

delete from zdb_active_source where zactvs_zdb_id = "ZDB-JRNL-051107-3";

select * from journal where jrnl_name like "BBA%";

--commit work;

rollback work;

