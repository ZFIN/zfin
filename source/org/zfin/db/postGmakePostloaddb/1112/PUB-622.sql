--liquibase formatted sql
--changeset xshao:PUB-622

delete from int_data_supplier
 where idsup_supplier_zdb_id like 'ZDB-LAB%'
   and idsup_supplier_zdb_id not in ('ZDB-LAB-991005-53','ZDB-LAB-130607-1','ZDB-LAB-130226-1','ZDB-LAB-990120-1');

