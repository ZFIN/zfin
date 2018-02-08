--liquibase formatted sql
--changeset staylor:ONT-638

create table marker_go_term_annotation_extension (
       mgtae_pk_id serial8 not null constraint mgtae_pk_id_not_null,
       mgtae_mrkrgoev_zdb_id varchar(50) not null constraint mgtae_mrkrgoev_zdb_id_not_null,
       mgtae_relationship_term_zdb_id varchar(50) not null constraint mgtae_relationship_term_zdb_id_not_null,
       mgtae_identifier_term_zdb_id varchaR(50) not null constraint mgtae_identifier_term_zdb_id_not_null

)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 51200 next size 51200;

create unique index mgrae_pk_index on marker_go_term_annotation_extension (mgtae_pk_id) using btree in idxdbs2;

create unique index mgrae_ak_index on marker_go_term_annotation_extension (mgtae_mrkrgoev_zdb_id, mgtae_relationship_term_zdb_id, mgtae_identifier_term_zdb_id) using btree in idxdbs1;

create index mgrae_rterm_zdb_id_fk_index
 on marker_go_term_annotation_extension (mgtae_relationship_term_zdb_id)
using btree in idxdbs2;

create index mgrae_iterm_zdb_id_fk_index
 on marker_go_term_annotation_extension (mgtae_identifier_term_zdb_id)
using btree in idxdbs1;

alter table marker_go_term_annotation_extension 
 add constraint primary key (mgtae_pk_id)
constraint marker_go_term_annotation_extension_pk;

alter table marker_go_term_annotation_extension
 add constraint (foreign key (mgtae_mrkrgoev_zdb_id)
 references marker_go_term_evidence on delete cascade constraint mgtae_mrkrgoev_zdb_id_fk_odc);

alter table marker_go_term_annotation_extension
 add constraint (foreign key (mgtae_relationship_term_zdb_id)
 references term on delete cascade constraint mgtae_relationship_term_zdb_id_fk_odc);

alter table marker_go_term_annotation_extension
 add constraint (foreign key (mgtae_identifier_term_zdb_id)
 references term on delete cascade constraint mgtae_identifier_term_zdb_id_fk_odc);

alter table marker_go_term_annotation_extension
 add constraint unique (mgtae_mrkrgoev_zdb_id,mgtae_relationship_term_zdb_id,mgtae_identifier_term_zdb_id) constraint mgtae_ak;

