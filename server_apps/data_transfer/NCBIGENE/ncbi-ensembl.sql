Insert into foreign_db_contains values ('Mouse', get_id('FDBCONT'),11,null,64);
Insert into foreign_db_contains values ('Human', get_id('FDBCONT'),11,null,64);

DROP TABLE if exists tmp_ncbi_ensembl;

CREATE TABLE tmp_ncbi_ensembl (
    taxon_id text,
    gene_id text,
    gene_symbol text,
    ncbi_id text,
    ensembl_id text
);

\copy tmp_ncbi_ensembl FROM 'ncbi_ensembl.tsv' WITH delimiter E'\t';

-- remove prefix of taxon IDs
update tmp_ncbi_ensembl set taxon_id = (replace(taxon_id,'NCBITaxon:',''));

select * from tmp_ncbi_ensembl limit 5;

-- remove import entries that already exist by Ensembl ID
delete from tmp_ncbi_ensembl where
exists (
select * from ortholog_external_reference, ortholog where
 oef_ortho_zdb_id = ortho_zdb_id AND
 ortho_other_species_taxid = taxon_id::integer AND
 oef_accession_number = ensembl_id
);

-- remove import entries that do not have a ncbi ID
delete from tmp_ncbi_ensembl where
not exists (
select 'x' from ortholog_external_reference, ortholog where
 oef_ortho_zdb_id = ortho_zdb_id AND
 ortho_other_species_taxid = taxon_id::integer AND
 oef_accession_number = ncbi_id
);

drop table tmp_ortholog_external_reference;

create table tmp_ortholog_external_reference (
ortho_id text,
accession text,
fdbcont_id text
);

insert into tmp_ortholog_external_reference
select oef_ortho_zdb_id, ensembl_id,
case
when taxon_id = '10090' then (select fdbcont_zdb_id from foreign_db_contains where fdbcont_organism_common_name = 'Mouse' and fdbcont_fdb_db_id = 64)
when taxon_id = '9606' then (select fdbcont_zdb_id from foreign_db_contains where fdbcont_organism_common_name = 'Human' and fdbcont_fdb_db_id = 64)
END
 from ortholog_external_reference, tmp_ncbi_ensembl, ortholog
where ncbi_id = oef_accession_number
AND ortho_zdb_id = oef_ortho_zdb_id
AND ortho_other_species_taxid = taxon_id::integer
;

select count(*) from tmp_ortholog_external_reference;

select * from tmp_ortholog_external_reference limit 5;

insert into ortholog_external_reference
select * from tmp_ortholog_external_reference;

