--liquibase formatted sql
--changeset sierra:protein_tables.sql

create table protein (up_uniprot_id text not null primary key,
                              up_fdbcont_zdb_id text not null,    
                              up_length int);


alter table protein 
add constraint protein_zdb_active_data_fk_odc
foreign key (up_uniprot_id) references zdb_active_data on delete cascade;

alter table protein
 add constraint protein_fdbcont_zdb_id_fk
 foreign key (up_fdbcont_zdb_id) references foreign_db_contains;


create table interpro_protein (ip_interpro_id text not null primary key,
                               ip_name text not null,
                               ip_type text not null);

alter table interpro_protein 
 add constraint interpro_protein_zdb_active_data_fk_odc
 foreign key (ip_interpro_id) references zdb_active_data on delete cascade;

alter table interpro_protein
 add constraint interpro_protein_ak unique (ip_interpro_id, ip_name, ip_type);

create table protein_to_interpro (pti_pk_id serial8 not null primary key,
                                  pti_uniprot_id text not null,
                                  pti_interpro_id text not null,
                                  pti_domain_start int not null,
                                  pti_domain_end int not null);
alter table protein_to_interpro
 add constraint protein_to_interpro_ak unique (pti_uniprot_id, pti_interpro_id, pti_domain_start, pti_domain_end);

alter table protein_to_interpro
 add constraint protein_to_interpro_uniprot_id foreign key (pti_uniprot_id)
 references protein on delete cascade;

alter table protein_to_interpro
 add constraint protein_to_interpro_interpro_id foreign key (pti_interpro_id)
 references interpro_protein on delete cascade;

create table marker_to_protein (mtp_pk_id serial8 not null primary key,
                                mtp_mrkr_zdb_id text not null,
                                mtp_uniprot_id text not null);


alter table marker_to_protein
 add constraint marker_to_protein_uniprot_id foreign key (mtp_uniprot_id)
 references protein on delete cascade;

alter table marker_to_protein
 add constraint protein_to_interpro_interpro_id foreign key (mtp_mrkr_zdb_id)
 references marker on delete cascade;


