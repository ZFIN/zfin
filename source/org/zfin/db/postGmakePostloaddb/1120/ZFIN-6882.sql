--liquibase formatted sql
--changeset pm:ZFIN-6882
--fixing ENSG ids for human orthologs (work initially done as a part of ZFIN-6604)

--table tmp_ortholog_reference created as a part of ZFIN-6604
delete * from tmp_ortholog_external_reference  where accession like 'ENSG%';

insert into tmp_ortholog_external_reference
select distinct oef_ortho_zdb_id, ensembl_id,
'ZDB-FDBCONT-200602-2'
 from ortholog_external_reference, tmp_ncbi_ensembl, ortholog
where ncbi_id = oef_accession_number and ensembl_id like 'ENSG%'
AND ortho_zdb_id = oef_ortho_zdb_id
AND ortho_other_species_taxid = taxon_id::integer and  ortho_other_species_ncbi_gene_id=oef_accession_number;

delete  from ortholog_external_reference where oef_accession_number like 'ENSG%';

insert into ortholog_external_reference
select distinct * from tmp_ortholog_external_reference where accession like 'ENSG%';

drop table if exists tmp_ortholog_external_reference;

drop table if exists tmp_ncbi_ensembl;