--liquibase formatted sql
--changeset xshao:JENK-338

create temp table new_annual_stats 
  (
    t_date varchar(50) not null,
    t_section varchar(255) not null,
    t_type varchar(255) not null,
    t_count integer not null
  ) with no log;

load from new_stats.unl insert into new_annual_stats;

insert into annual_stats (as_count, as_section, as_type, as_date)
  select t_count, t_section, t_type, TO_DATE(t_date, "%Y-%m-%d")
    from new_annual_stats; 

