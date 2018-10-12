--liquibase formatted sql
--changeset sierra:CUR-846.sql

alter table omim_phenotype
 add omimp_human_gene_id text;

create table human_gene_detail (hgd_gene_id text not null ,
                                hgd_gene_name text,
                                hgd_gene_symbol text not null)
;

create unique index human_gene_detail_pk_index 
 on human_gene_detail (hgd_gene_id);

alter table human_gene_detail
 add constraint human_gene_detail_primary_key
 primary key using index human_gene_detail_pk_index;

create index omim_phenotype_human_gene_id_index 
 on human_gene_detail (hgd_gene_id);

alter table omim_phenotype
 add constraint omim_phenotype_human_gene_id_fk 
 foreign key (omimp_human_gene_id)
 references human_gene_detail on delete cascade;
