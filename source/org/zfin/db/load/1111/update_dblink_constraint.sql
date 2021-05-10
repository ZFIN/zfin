--liquibase formatted sql
--changeset sierra:update_dblink_constraint.sql

alter table db_link
 alter column dblink_acc_num_display
  drop not null;

alter table db_link
  add constraint dblink_acc_num_display_check check (dblink_fdbcont_zdb_id != 'ZDB-FDBCONT-141007-1' or dblink_acc_num_display is not null);

