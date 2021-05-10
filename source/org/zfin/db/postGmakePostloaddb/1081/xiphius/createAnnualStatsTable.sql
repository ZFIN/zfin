--liquibase formatted sql
--changeset sierra:createAnnualStatsTable

create table annual_stats (as_pk_id serial8 not null constraint as_pk_id_not_null,
       	     		   as_count int not null constraint as_count_not_null,
			   as_section varchar(255) not null constraint as_section_not_null,
			   as_type varchar(255) not null constraint as_type_not_null,
			   as_date datetime year to second not null constraint as_year_not_null)
in tbldbs1
extent size 32 next size 32;

create unique index annual_stats_primary_key_index
 on annual_stats(as_pk_id)
using btree in idxdbs2;

alter table annual_stats 
  add constraint primary key (as_pk_id)
 constraint annual_stats_primary_key;

