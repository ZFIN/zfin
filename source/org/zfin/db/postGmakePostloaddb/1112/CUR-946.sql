--liquibase formatted sql
--changeset xshao:CUR-946

delete from zdb_active_data where exists(select * from ortholog where ortho_zdb_id = zactvd_zdb_id and ortho_zebrafish_gene_zdb_id = 'ZDB-GENE-050411-113');

