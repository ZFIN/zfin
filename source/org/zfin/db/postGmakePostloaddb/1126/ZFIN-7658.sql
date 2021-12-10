--liquibase formatted sql
--changeset cmpich:ZFIN-7658

-- remove sourcebioscoence as a BAC supplier
delete
from int_data_supplier
where idsup_data_zdb_id in (
    select mrkr_zdb_id
    from marker
    where mrkr_type = 'BAC'
)
  AND idsup_supplier_zdb_id in (
    select zdb_id from company where name = 'Source BioScience (formerly Imagenes)'
);
