--liquibase formatted sql
--changeset sierra:createAnnualStatsTable

create table annual_stats (as_pk_id serial8 not null constraint as_pk_id_not_null,
       	     		   as_count int not null constraint as_count_not_null,
			   as_section varchar(100) not null constraint as_section_not_null)
in tbldbs1
extent size 32 next size 32;

create unique index annual_stats_primary_key_index
 on annual_stats(as_pk_id)
using btree in idxdsb2;

create unique index annual_stats_section_index
 on annual_stats(as_section)
 using btree in idxdbs1;

alter table annual_stats 
  add constraint primary key (as_pk_id)
 constraint annual_stats_primary_key;

