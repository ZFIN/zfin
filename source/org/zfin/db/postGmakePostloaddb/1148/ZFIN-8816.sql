--liquibase formatted sql
--changeset cmpich:ZFIN-8816.sql

select convert_gene_to_ncrna('ZDB-GENE-030616-59');
select convert_gene_to_ncrna('ZDB-GENE-031010-46');
select convert_gene_to_ncrna('ZDB-GENE-041001-123');
select convert_gene_to_ncrna('ZDB-GENE-030131-2681');
select convert_gene_to_ncrna('ZDB-GENE-041210-148');
select convert_gene_to_ncrna('ZDB-GENE-070705-20');
select convert_gene_to_ncrna('ZDB-GENE-030131-7012');
