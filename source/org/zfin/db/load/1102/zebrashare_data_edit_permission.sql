--liquibase formatted sql
--changeset sierra:zebrashare_data_edit_permission


create table zebrashare_data_edit_permission (
       zdep_zdb_id text not null primary key,
       zdep_person_zdb_id text not null,
       zdep_pub_zdb_id text not null,
       zdep_person_is_submitter boolean default 'f')
;

create unique index zebrashare_alternate_key_index
  on zebrashare_data_edit_permission (zdep_person_zdb_id, zdep_pub_zdb_id);

create index zebrashare_person_fk_index
 on zebrashare_data_edit_permission(zdep_person_zdb_id);

create index zebrashare_pub_fk_index
 on zebrashare_data_edit_permission(zdep_pub_zdb_id);

alter table zebrashare_data_edit_permission 
add constraint zdep_person_fk foreign key (zdep_person_zdb_id) references person (zdb_id)
 on delete cascade; 

alter table zebrashare_data_edit_permission 
add constraint zdep_pub_fk foreign key (zdep_pub_zdb_id) references publication (zdb_id)
 on delete cascade; 

