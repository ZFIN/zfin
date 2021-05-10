--liquibase formatted sql
--changeset xshao:DLOAD-496

update ortholog set ortho_other_species_ncbi_gene_id = "330963" where ortho_other_species_ncbi_gene_id = "384945";
update ortholog_external_reference set oef_accession_number = "330963" where oef_accession_number = "384945";
update ortholog set ortho_other_species_ncbi_gene_id = "338320" where ortho_other_species_ncbi_gene_id = "217615";
update ortholog_external_reference set oef_accession_number = "338320" where oef_accession_number = "217615";
update ortholog set ortho_other_species_ncbi_gene_id = "105378803" where ortho_other_species_ncbi_gene_id = "100144878";
update ortholog_external_reference set oef_accession_number = "105378803" where oef_accession_number = "100144878";
update ortholog set ortho_other_species_ncbi_gene_id = "4253" where ortho_other_species_ncbi_gene_id = "117153";
update ortholog_external_reference set oef_accession_number = "4253" where oef_accession_number = "117153";
update ortholog set ortho_other_species_ncbi_gene_id = "100996634" where ortho_other_species_ncbi_gene_id = "389422";
update ortholog_external_reference set oef_accession_number = "100996634" where oef_accession_number = "389422";
update ortholog set ortho_other_species_ncbi_gene_id = "105375355" where ortho_other_species_ncbi_gene_id = "80761";
update ortholog_external_reference set oef_accession_number = "105375355" where oef_accession_number = "80761";
