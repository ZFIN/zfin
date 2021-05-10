--liquibase formatted sql
--changeset sierra:ZFIN-6075b.sql

alter table gene_description
 add constraint gd_gene_zdb_id_fk_odc foreign key (gd_gene_zdb_id)
 references marker(mrkr_zdb_id) on delete cascade; 

