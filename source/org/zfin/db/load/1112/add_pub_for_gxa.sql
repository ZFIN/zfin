--liquibase formatted sql
--changeset sierra:add_pub_for_gxa.sql

insert into zdb_active_source
 select 'ZDB-PUB-200103-6'
   from single 
where not exists (select 'x' from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-200103-6');

insert into publication (zdb_id, jtype, pub_jrnl_zdb_id, authors)
 select 'ZDB-PUB-200103-6', 'Curation','ZDB-JRNL-050621-1294','ZFIN Staff')
  from single 
where not exists (select 'x' from publication where zdb_id = 'ZDB-PUB-200103-6';


