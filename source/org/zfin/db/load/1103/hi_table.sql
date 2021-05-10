--liquibase formatted sql
--changeset sierra:hi_table.sql

create table amsterdam_file (af_pk_id serial8 not null,
                             af_feature_zdb_id text not null,
                             af_file_location text not null,
                             af_is_overlapping_file text)
;

create unique index af_pk_id_index
 on amsterdam_file (af_pk_id);

create unique index af_ak_index
 on amsterdam_file (af_feature_zdb_id, af_file_location)
 ;

alter table amsterdam_file 
 add constraint af_feature_zdb_id_fk foreign key (af_feature_zdb_id)
  references feature (feature_zdb_id) on delete cascade;


