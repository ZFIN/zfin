--liquibase formatted sql 
--changeset sierra:fixLogicalColumnGo.sql

alter table marker_go_term_annotation_extension
 drop mgtae_logical_operator ;


create table marker_go_term_annotation_extension_group (
       mgtaeg_mrkrgoev_zdb_id varchar(50) not null constraint mgtaeg_mrkrgoev_zdb_id_not_null,
       mgtaeg_annotation_extension_group_id serial8 not null constraint mgtaeg_annotation_extension_group_id_not_null,
       mgtaeg_logical_operator varchar(10))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 8192 next size 8192;

create unique index mgtaeg_pk_index 
  on marker_go_term_annotation_extension_group (mgtaeg_annotation_extension_group_id) 
  using btree in idxdbs2;

create index mgtaeg_fk_index 
  on marker_go_term_annotation_extension_group (mgtaeg_mrkrgoev_zdb_id)
  using btree in idxdbs3;

alter table marker_go_term_annotation_extension_group
  add constraint primary key (mgtaeg_annotation_extension_group_id)
 constraint mgtaeg_primary_key;

alter table marker_go_term_annotation_extension_group
 add constraint (foreign key  (mgtaeg_mrkrgoev_zdb_id)
 references marker_go_term_evidence  on delete cascade constraint
 mgtaeg_fk_odc);

alter table marker_go_term_annotation_extension
 add (mgtae_extension_group_id int8 not null constraint mgtae_extension_group_id_not_null);

create index mgtae_extension_group_id_index 
 on marker_go_term_annotation_extension (mgtae_extension_group_id)
 using btree in idxdbs1;

alter table marker_go_term_annotation_extension
 add constraint (foreign key (mgtae_extension_group_id)
 references marker_go_term_annotation_extension_group
 on delete cascade constraint mgtae_extension_group_id_fk_odc);

