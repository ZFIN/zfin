--liquibase formatted sql
--changeset sierra:student_checklist.sql

create table processing_checklist_task (pct_pk_id serial8 not null primary key,
                                        pct_task text not null);

create unique index pct_task_index
  on processing_checklist_task (pct_task);


create table publication_processing_checklist (ppc_pk_id serial8 not null primary key,
       ppc_pub_zdb_id text not null,
       ppc_person_zdb_id text not null,
       ppc_date_completed timestamp without time zone default now(),
       ppc_task_id bigint not null)
;

create unique index ppc_primary_key_index
 on publication_processing_checklist (ppc_pub_zdb_id, ppc_task);

create index ppc_person_fk_index 
  on publication_processing_checklist (ppc_person_zdb_id);

create index ppc_pub_fk_index 
  on publication_processing_checklist (ppc_pub_zdb_id);

alter table publication_processing_checklist 
  add constraint ppc_person_zdb_id_fk
  foreign key (ppc_person_zdb_id)
  references person;

alter table publication_processing_checklist 
  add constraint ppc_pub_zdb_id_fk
  foreign key (ppc_pub_zdb_id)
  references publication;

create index ppc_task_id_fk_index
  on publication_processing_checklist (ppc_task_id)
  ;

alter table publication_processing_checklist
  add constraint ppc_task_id_fk
  foreign key (ppc_task_id)
  references processing_checklist_task;

