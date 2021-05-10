--liquibase formatted sql
--changeset sierra:migrateStats

insert into annual_stats (as_count, as_section, as_type, as_date)
 select counter, section, type, date
   from tmp_stats;
