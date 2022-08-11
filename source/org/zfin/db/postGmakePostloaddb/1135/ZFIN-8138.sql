--liquibase formatted sql
--changeset cmpich:ZFIN-8138

delete
from int_data_supplier
where idsup_data_zdb_id like 'ZDB-ALT%'
  and exists(
        select 1
        from lab
        where idsup_supplier_zdb_id = zdb_id
          and name = 'Baier Lab'
    );
