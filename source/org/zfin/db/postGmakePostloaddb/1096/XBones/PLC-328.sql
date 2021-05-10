--liquibase formatted sql
--changeset pm:PLC-328

update  company set owner=null where zdb_id='ZDB-COMPANY-121002-1';
delete from zdb_active_source where zactvs_zdb_id='ZDB-PERS-121002-11';