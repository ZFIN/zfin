--liquibase formatted sql
--changeset sierra:clean_up_zdb_replaced_data.sql


delete from zdb_replaced_data 
 where zrepld_old_zdb_id = zrepld_new_zdb_id;


