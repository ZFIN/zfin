--liquibase formatted sql
--changeset xshao:DLOAD-496

update ortholog set ortho_other_species_ncbi_gene_id = "330963" where ortho_other_species_ncbi_gene_id = "384945";
update ortholog set ortho_other_species_ncbi_gene_id = "338320" where ortho_other_species_ncbi_gene_id = "217615";
update ortholog set ortho_other_species_ncbi_gene_id = "105378803" where ortho_other_species_ncbi_gene_id = "100144878";
update ortholog set ortho_other_species_ncbi_gene_id = "4253" where ortho_other_species_ncbi_gene_id = "117153";
update ortholog set ortho_other_species_ncbi_gene_id = "100996634" where ortho_other_species_ncbi_gene_id = "389422";
update ortholog set ortho_other_species_ncbi_gene_id = "105375355" where ortho_other_species_ncbi_gene_id = "80761";
