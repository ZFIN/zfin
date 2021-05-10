--liquibase formatted sql
--changeset xshao:JENK-338
insert into annual_stats (as_count, as_section, as_type, as_date)
  select t_count, t_section, t_type, TO_DATE(t_date, "%Y-%m-%d")
    from tmp_annual_stats; 
drop table tmp_annual_stats;
