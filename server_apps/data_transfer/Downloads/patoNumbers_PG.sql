begin work;

-- prepare phenotype data:
-- add term_ont_ids and names and post-composed relationships

--! echo "Create table to hold phenotype data"
create temp table tmp_phenotype_statement (
       phenos_pk_id bigint,
       asuperterm_ont_id text,
       asuperterm_name text,
       arelationship_id text,
       arelationship_name text,
       asubterm_ont_id text,
       asubterm_name text,
       bsuperterm_ont_id text,
       bsuperterm_name text,
       brelationship_id text,
       brelationship_name text,
       bsubterm_ont_id text,
       bsubterm_name text,
       quality_id text,
       quality_name text,
       quality_tag text,
       a_ontology_name text,
       b_ontology_name text
);

--! echo "Insert all phenotype data with term_ont_ids "
insert into tmp_phenotype_statement (phenos_Pk_id,asubterm_ont_id, asubterm_name,asuperterm_ont_id, asuperterm_name,
bsubterm_ont_id, bsubterm_name,bsuperterm_ont_id, bsuperterm_name, quality_id, quality_name, a_ontology_name,b_ontology_name,quality_tag)
  select phenos_Pk_id, asubterm.term_ont_id, asubterm.term_name, asuperterm.term_ont_id as id2, asuperterm.term_name as name2,
         bsubterm.term_ont_id as id3, bsubterm.term_name as name3, bsuperterm.term_ont_id as id4, bsuperterm.term_name as name4,
         quality.term_ont_id as id5, quality.term_name as name5,  asubterm.term_ontology, bsubterm.term_ontology as ont2, phenos_tag
    from phenotype_statement
     full outer join term as asubterm
                  on asubterm.term_zdb_id = phenos_entity_1_subterm_zdb_id
                join term as asuperterm
                  on asuperterm.term_zdb_id = phenos_entity_1_superterm_zdb_id
     full outer join term as bsubterm
                  on bsubterm.term_zdb_id = phenos_entity_2_subterm_zdb_id
     full outer join term as bsuperterm
                  on bsuperterm.term_zdb_id = phenos_entity_2_superterm_zdb_id
                join term as quality
                  on quality.term_zdb_id = phenos_quality_zdb_id;

create index tmp_phenotype_statement_index
  on tmp_phenotype_statement (phenos_pk_id);


--! echo "update a relationship name"
update tmp_phenotype_statement
  set arelationship_name =
(
case
when (asubterm_ont_id is not null AND a_ontology_name = 'biological_process')
then
'occurs_in'
when (asubterm_ont_id is not null AND a_ontology_name != 'biological_process')
then
'part_of'
else
null
end
);

--! echo "update a relationship ID"
update tmp_phenotype_statement
  set arelationship_id =
(
case
when (asubterm_ont_id is not null AND a_ontology_name = 'biological_process')
then
'BFO:0000066'
when (asubterm_ont_id is not null AND a_ontology_name != 'biological_process')
then
'BFO:0000050'
else
null
end
);

--! echo "update b relationship name"
update tmp_phenotype_statement
  set brelationship_name =
(
case
when (bsubterm_ont_id is not null AND b_ontology_name = 'biological_process')
then
'occurs_in'
when (bsubterm_ont_id is not null AND b_ontology_name != 'biological_process')
then
'part_of'
else
null
end
);

--! echo "update b relationship ID"
update tmp_phenotype_statement
  set brelationship_id =
(
case
when (bsubterm_ont_id is not null AND b_ontology_name = 'biological_process')
then
'BFO:0000066'
when (bsubterm_ont_id is not null AND b_ontology_name != 'biological_process')
then
'BFO:0000050'
else
null
end
);

create temp table tmp_pheno_genes (gene_id text);

insert into tmp_pheno_genes
  select distinct fc_gene_zdb_id
    from fish_components, fish_experiment, mutant_fast_Search
    where fc_fish_zdb_id = genox_fish_zdb_id
    and mfs_genox_zdb_id = genox_zdb_id
    and mfs_mrkr_zdb_id = fc_gene_zdb_id; 

insert into tmp_pheno_genes
  select distinct fc_gene_zdb_id
    from fish_components, fish_experiment, mutant_fast_Search
    where fc_fish_zdb_id = genox_fish_zdb_id
    and mfs_genox_zdb_id = genox_zdb_id
    and mfs_mrkr_zdb_id = fc_affector_zdb_id ;

create temp table tmp_unique (gene_id text);

insert into tmp_unique
select distinct gene_id from tmp_pheno_genes ;

create unique index gene_id_p on tmp_unique 
  (gene_id);

create temp table tmp_o_with_p as
select distinct s.gene_id, ortho_zdb_id, ortho_other_species_symbol, organism_common_name
  from tmp_unique s, ortholog, organism
  where ortho_zebrafish_gene_zdb_id = s.gene_id
  and organism_taxid = ortho_other_species_taxid
  and organism_common_name = 'Human'
;

create temp table tmp_ortho_pheno as
select ortho_zdb_id, gene_id, mrkr_abbrev, ortho_other_Species_symbol, organism_common_name,
	 a.term_ont_id as a_ont_id,a.term_name as e1superName, a.term_ontology as e1superTermOntology,
  	 b.term_ont_id as b_ont_id,b.term_name as e1subName, 
	 b.term_ontology as e1subTermOntology, c.term_ont_id as c_ont_id,
	 c.term_name as e2supername, c.term_ontology as e2superTermOntology,
	 d.term_ont_id as d_ont_id,d.term_name as e2subname, d.term_ontology as e2subTermOntology,
	 e.term_ont_id as e_ont_id, e.term_name as qualityName, phenos_tag,
	 '' as a_relationship_id,'' as a_relationship_name,'' as b_relationship_id,'' as b_relationship_name, phenos_pk_id as phenos_id
    from tmp_o_with_p 
    join feature_marker_relationship on fmrel_mrkr_zdb_id = gene_id
    join genotype_feature on fmrel_ftr_zdb_id = genofeat_feature_zdb_id
    join fish on genofeat_geno_zdb_id = fish_genotype_zdb_id
    join fish_experiment on fish_zdb_id = genox_fish_zdb_id
    join phenotype_experiment on phenox_genox_zdb_id = genox_zdb_id
    join phenotype_statement on phenox_pk_id = phenos_phenox_pk_id
    join marker on fmrel_mrkr_Zdb_id = mrkr_zdb_id
    join term a on a.term_zdb_id = phenos_entity_1_superterm_zdb_id
    full outer join term b on b.term_zdb_id = phenos_entity_1_subterm_zdb_id
    full outer join term c on c.term_zdb_id = phenos_entity_2_superterm_zdb_id
    full outer join term d on d.term_zdb_id = phenos_entity_2_subterm_zdb_id
    full outer join term e on e.term_zdb_id = phenos_quality_zdb_id
    join mutant_fast_search on genox_zdb_id = mfs_genox_zdb_id
    where fmrel_type = 'is allele of'
      and mfs_mrkr_zdb_id = mrkr_zdb_id
      and phenos_tag != 'normal'
union
  select ortho_zdb_id, gene_id,  mrkr_abbrev, ortho_other_species_symbol, organism_common_name,
	 a.term_ont_id as a_ont_id,a.term_name as e1superName, a.term_ontology as e1superTermOntology,
  	 b.term_ont_id as b_ont_id,b.term_name as e1subName, 
	 b.term_ontology as e1subTermOntology, c.term_ont_id as c_ont_id,
	 c.term_name as e2supername, c.term_ontology as e2superTermOntology,
	 d.term_ont_id as d_ont_id,d.term_name as e2subname, d.term_ontology as e2subTermOntology,
	 e.term_ont_id as e_ont_id, e.term_name as qualityName, phenos_tag,
	 '' as a_relationship_id,'' as a_relationship_name,'' as b_relationship_id,'' as b_relationship_name, phenos_pk_id as phenos_id
    from tmp_o_with_p 
    join marker_relationship on mrel_mrkr_2_zdb_id = gene_id
    join fish_str on mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
    join fish_experiment on fishstr_fish_zdb_id = genox_fish_zdb_id
    join phenotype_Experiment on genox_zdb_id = phenox_genox_zdb_id
    join phenotype_statement on phenox_pk_id = phenos_phenox_pk_id
    join marker on mrkr_zdb_id = mrel_mrkr_2_zdb_id
    join term a on a.term_Zdb_id = phenos_entity_1_superterm_zdb_id 
    full outer join term b on b.term_zdb_id = phenos_entity_1_subterm_zdb_id
    full outer join term c on c.term_zdb_id = phenos_entity_2_superterm_zdb_id
    full outer join term d on d.term_zdb_id = phenos_entity_2_subterm_zdb_id
    full outer join term e on e.term_zdb_id = phenos_quality_zdb_id
    join mutant_fast_search on mfs_genox_zdb_id = genox_zdb_id
    where mfs_mrkr_zdb_id = mrkr_zdb_id
      and mfs_mrkr_zdb_id like 'ZDB-GENE%'
      and phenos_tag != 'normal'
;

select count(*) from tmp_ortho_pheno;

create index tmp_ortho_pheno_index
  on tmp_ortho_pheno (phenos_id);


\copy (select distinct gene_id,  mrkr_abbrev from tmp_ortho_pheno order by gene_id) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/test_preprocessed_pheno.txt' with delimiter as '|' null as '';

create view preprocessedPheno as
  select distinct ortho_zdb_id, gene_id, mrkr_abbrev,a_ont_id,e1superName, b_ont_id,e1subName, 
  	 c_ont_id,e2superName,d_ont_id,e2subName,e_ont_id,qualityName, phenos_tag,
  	 arelationship_id, arelationship_name, brelationship_id, brelationship_name,quality_id
    from tmp_ortho_pheno, tmp_phenotype_statement
    where phenos_id = phenos_pk_id
    order by gene_id, mrkr_abbrev;
\copy (select * from preprocessedPheno) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/preprocessed_pheno.txt'  with delimiter as '|' null as '';
drop view preprocessedPheno;

create view preprocessedOrtho as
  select distinct ortho_zdb_id, gene_id, mrkr_abbrev, ortho_other_species_symbol
     from tmp_ortho_pheno
    order by gene_id, mrkr_abbrev, ortho_other_species_symbol;
\copy (select * from preprocessedOrtho) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/preprocessed_ortho.txt' with delimiter as '|' null as '';
drop view preprocessedOrtho;

commit work;
--rollback work ;
