--liquibase formatted sql
--changeset sierra:zebrashare_metadata.sql

create table zebrashare_submission_metadata
  (zsm_pub_zdb_id text not null primary key,
   zsm_submitter_zdb_id text not null,
   zsm_lab_of_origin_zdb_id text not null);

create index zsm_pub_zdb_id_fk_index 
  on zebrashare_submission_metadata (zsm_pub_zdb_id);

create index zsm_submitter_zdb_id_fk_index 
  on zebrashare_submission_metadata (zsm_submitter_zdb_id);

alter table zebrashare_submission_metadata
 add constraint zem_pub_fk foreign key (zsm_pub_zdb_id)
 references publication (zdb_id) on delete cascade; 

alter table zebrashare_submission_metadata
 add constraint zem_submitter_fk foreign key (zsm_submitter_zdb_id)
 references person (zdb_id) on delete cascade; 

alter table zebrashare_submission_metadata
 add constraint zem_lab_fk foreign key (zsm_lab_of_origin_zdb_id)
 references lab (zdb_id) on delete cascade; 
