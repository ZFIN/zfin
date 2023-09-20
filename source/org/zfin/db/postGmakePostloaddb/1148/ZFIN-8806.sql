--liquibase formatted sql
--changeset cmpich:ZFIN-8806.sql

delete from DB_LINK where dblink_acc_num in ('AI558578','AI545034');
