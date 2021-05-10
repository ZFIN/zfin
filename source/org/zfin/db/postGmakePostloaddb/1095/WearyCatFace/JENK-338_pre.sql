--liquibase formatted sql
--changeset xshao:JENK-338

create table tmp_annual_stats 
  (
    t_date varchar(50),
    t_section varchar(255),
    t_type varchar(255),
    t_count varchar(50)
  );

