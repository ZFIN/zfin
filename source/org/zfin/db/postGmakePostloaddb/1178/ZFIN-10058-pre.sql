--liquibase formatted sql
--changeset cmpich:ZFIN-10058-pre

ALTER TABLE gene_allele_mutation_detail ADD COLUMN IF NOT EXISTS exons varchar(255);
ALTER TABLE gene_allele_mutation_detail ADD COLUMN IF NOT EXISTS introns varchar(255);
ALTER TABLE gene_allele_mutation_detail ADD COLUMN IF NOT EXISTS alias varchar(255);
ALTER TABLE gene_allele_mutation_detail ADD COLUMN IF NOT EXISTS type varchar(255);
ALTER TABLE crispr_detail ADD COLUMN IF NOT EXISTS type varchar(255);

delete from gene_allele_mutation_detail;

delete from crispr_detail;

