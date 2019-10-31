--liquibase formatted sql
--changeset sierra:add_organism_pk.sql

create table ncbi_ortholog_alias ( noa_pk_id serial8 not null primary key,
noa_ncbi_gene_id text not null,
noa_alias text not null
);

create unique index ncbi_ortho_alias_alternate_key_index
 on ncbi_ortholog_alias (noa_ncbi_gene_id, noa_alias) ;

create index noa_ncbi_gene_id_index on ncbi_ortholog_alias
  (noa_ncbi_gene_id);

alter table ncbi_ortholog_alias
  add constraint noa_ncbi_gene_id_fk
 foreign key (noa_ncbi_gene_id)
 references ncbi_ortholog (noi_ncbi_gene_id);




 

