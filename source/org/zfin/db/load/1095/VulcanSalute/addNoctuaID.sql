--liquibase formatted sql
--changeset sierra:addNoctuaID.sql

create table noctua_model (nm_id varchar(255) not null constraint nm_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 4096 next size 4096;

create unique index nm_id_pk_index
 on noctua_model (nm_id)
 using btree in idxdbs2;

alter table noctua_model
 add constraint primary key (nm_id)
 constraint noctua_model_primary_key;

create table noctua_model_annotation (nma_pk_id serial8 not null constraint nma_pk_id_not_null,
				       nma_nm_id varchar(255) not null constraint nma_nm_id_not_null,
       	     			       nma_mrkrgoev_zdb_id varchar(50) not null constraint nma_mrkrgoev_zdb_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 8192 next size 8192;

create index nma_nm_id_fk_index
 on noctua_model_annotation (nma_nm_id)
 using btree in idxdbs3;

create index nma_mrkrgoev_zdb_id_fk_index
 on noctua_model_annotation (nma_mrkrgoev_zdb_id)
 using btree in idxdbs1;

create unique index nma_pk_index 
 on noctua_model_annotation (nma_pk_id)
 using btree in idxdbs1;

create unique index nma_ak_index 
 on noctua_model_annotation (nma_nm_id, nma_mrkrgoev_zdb_id)
 using btree in idxdbs3;

alter table noctua_model_annotation
 add constraint  (foreign key (nma_nm_id)
 references noctua_model on delete cascade constraint nma_noctua_model_fk_odc);

alter table noctua_model_annotation
 add constraint  (foreign key (nma_mrkrgoev_zdb_id)
 references marker_go_term_evidence on delete cascade constraint nma_mrkrgoev_fk_odc);

alter table noctua_model_annotation
 add constraint primary key (nma_pk_id)
 constraint noctua_model_annotation_pk;

alter table noctua_model_annotation
 add constraint unique (nma_nm_id, nma_mrkrgoev_zdb_id)
 constraint noctual_model_annotation_ak;
