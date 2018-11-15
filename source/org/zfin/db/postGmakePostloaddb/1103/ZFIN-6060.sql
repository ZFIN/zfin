--liquibase formatted sql
--changeset pm:ZFIN-6060


delete from  int_data_supplier  where idsup_supplier_zdb_id='ZDB-LAB-980202-4';