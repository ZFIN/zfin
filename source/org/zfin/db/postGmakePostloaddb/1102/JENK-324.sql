--liquibase formatted sql
--changeset xshao:JENK-324

create temp table diseases_stats (d_date timestamp);

insert into diseases_stats select distinct as_date from annual_stats where as_date != '2018-08-30 13:24:01.911475';

alter table diseases_stats
  add column d_section text,
  add column d_type text,
  add column d_ct integer;

update diseases_stats
   set d_section = 'Expression & Phenotype',
       d_type = 'Diseases with Models',
       d_ct = 0;

update diseases_stats
   set d_ct = 281
 where d_date = '2018-01-01 00:00:00';

insert into annual_stats(as_date, as_section, as_type, as_count)
select d_date, d_section, d_type, d_ct from diseases_stats;                   

create temp table model_stats (m_date timestamp);

insert into model_stats select distinct as_date from annual_stats where as_date != '2018-08-30 13:24:01.911475';

alter table model_stats
  add column m_section text,
  add column m_type text,
  add column m_ct integer;

update model_stats
   set m_section = 'Expression & Phenotype',
       m_type = 'Disease Models',
       m_ct = 0;

update model_stats
   set m_ct = 839
 where m_date = '2018-01-01 00:00:00';

insert into annual_stats(as_date, as_section, as_type, as_count)
select m_date, m_section, m_type, m_ct from model_stats;      

create temp table eap_stats (e_date timestamp);

insert into eap_stats select distinct as_date from annual_stats where as_date != '2018-08-30 13:24:01.911475';

alter table eap_stats
  add column e_section text,
  add column e_type text,
  add column e_ct integer;

update eap_stats
   set e_section = 'Expression & Phenotype',
       e_type = 'Expression Phenotype',
       e_ct = 0;

update eap_stats
   set e_ct = 9346
 where e_date = '2018-01-01 00:00:00';

insert into annual_stats(as_date, as_section, as_type, as_count)
select e_date, e_section, e_type, e_ct from eap_stats;


