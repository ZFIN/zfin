--liquibase formatted sql
--changeset xshao:DLOAD-517

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ORTHO-180105-3';
update ortholog set ortho_other_species_ncbi_gene_id = '100534287' where ortho_other_species_ncbi_gene_id = '229459';
update ortholog_external_reference set oef_accession_number = '100534287' where oef_accession_number = '229459';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ORTHO-180105-4';
update ortholog set ortho_other_species_ncbi_gene_id = "221262" where ortho_other_species_ncbi_gene_id = '100996634';
update ortholog_external_reference set oef_accession_number = "221262" where oef_accession_number = '100996634';


