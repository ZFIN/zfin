--liquibase formatted sql
--changeset xshao:PLC-334

delete from int_data_supplier where idsup_supplier_zdb_id = 'ZDB-LAB-970424-15';
