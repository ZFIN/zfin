--liquibase formatted sql
--changeset sierra:12594

alter table probe_library
  modify (probelib_non_zfin_tissue_name varchar(150));

alter table  probe_library
  modify (probelib_sex varchar(15));

alter table stage
  modify (stg_other_features varchar(50));

alter table company 
 modify (phone varchar(100));

