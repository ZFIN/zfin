--liquibase formatted sql
--changeset sierra:15111

create table zdb_replaced_data_new
  (
    zrepld_old_zdb_id varchar(50) not null constraint zrepld_old_zdb_id_not_null,
    zrepld_new_zdb_id varchar(50) not null constraint zrepld_new_zdb_id_not_null,
    zrepld_old_name varchar(255),
    zrepld_pk_id serial8 not null constraint zrepld_pk_id_not_null
  );

insert into zdb_replaced_data_new (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
 select zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name
  from zdb_replaced_data;

drop table zdb_replaced_data;

rename table zdb_replaced_data_new to zdb_replaced_data;

create unique index nzdb_replaced_data_primary_key_index 
    on zdb_replaced_data (zrepld_old_zdb_id,zrepld_new_zdb_id) 
    using btree in idxdbs2;
create index nzrepld_new_zdb_id_index on zdb_replaced_data 
    (zrepld_new_zdb_id) using btree in idxdbs3;
alter table zdb_replaced_data add constraint primary 
    key (zrepld_old_zdb_id,zrepld_new_zdb_id) constraint nzdb_replaced_data_primary_key  ;


alter table zdb_replaced_data add constraint (foreign 
    key (zrepld_new_zdb_id) references zdb_active_data 
     on delete cascade constraint nzrepld_new_zdb_id_foreign_key);
