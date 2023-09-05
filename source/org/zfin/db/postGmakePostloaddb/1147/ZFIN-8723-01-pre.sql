--liquibase formatted sql
--changeset rtaylor:ZFIN-8723-01-pre.sql

-- CREATE TEMP TABLE TO HOLD DATA FIXES
DROP TABLE IF EXISTS temp_8723;
CREATE TABLE temp_8723 (
   "dblink_linked_recid" varchar(255),
   "dblink_acc_num" varchar(255),
   "dblink_info" varchar(255),
   "dblink_zdb_id" varchar(255),
   "dblink_acc_num_display" varchar(255),
   "dblink_length" varchar(255),
   "dblink_fdbcont_zdb_id" varchar(255),
   "incoming_info" varchar(255),
   "incoming_length" varchar(255),
   "incoming_length_matches" varchar(255)
);

