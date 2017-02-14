--liquibase formatted sql
--changeset sierra:15111_continued


create unique index zdb_replaced_data_primary_key_index 
    on zdb_replaced_data (zrepld_pk_id) 
    using btree  in idxdbs2;

create unique index zdb_replaced_data_alternate_key_index 
    on zdb_replaced_data (zrepld_old_zdb_id,zrepld_new_zdb_id) 
    using btree  in idxdbs2;
create index zrepld_new_zdb_id_index on zdb_replaced_data 
    (zrepld_new_zdb_id) using btree  in idxdbs3;
alter table zdb_replaced_data add constraint primary 
    key (zrepld_old_zdb_id,zrepld_new_zdb_id) constraint
    zdb_replaced_data_primary_key  ;


alter table zdb_replaced_data add constraint (foreign 
    key (zrepld_new_zdb_id) references zdb_active_data 
     on delete cascade constraint zrepld_new_zdb_id_foreign_key);
