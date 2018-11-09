--liquibase formatted sql
--changeset sierra:add_zdb_id.sql

insert into zdb_object_type (zobjtype_name,
       zobjtype_day,
       zobjtype_home_table,
       zobjtype_home_zdb_id_column,
       zobjtype_is_data,
       zobjtype_is_source)
values ('ZDEP',NOW(),'zebrashare_data_edit_permission','zdep_zdb_id',
        't','f');

create sequence zdepseq_seq increment by 1
minvalue 1
start with 1;
