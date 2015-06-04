begin work;

-- prepare phenotype data:
-- add term_ont_ids and names and post-composed relationships

! echo "Create table to hold phenotype data"
create temp table tmp_phenotype_statement (
       phenos_pk_id serial,
       asuperterm_ont_id varchar(30),
       asuperterm_name varchar(255),
       arelationship_id varchar(30),
       arelationship_name varchar(30),
       asubterm_ont_id varchar(30),
       asubterm_name varchar(255),
       bsuperterm_ont_id varchar(30),
       bsuperterm_name varchar(255),
       brelationship_id varchar(30),
       brelationship_name varchar(30),
       bsubterm_ont_id varchar(30),
       bsubterm_name varchar(255),
       quality_id varchar(30),
       quality_name varchar(255),
       quality_tag varchar(20),
       a_ontology_name varchar(50),
       b_ontology_name varchar(50)
)
with no log;

! echo "Insert all phenotype data with term_ont_ids "
insert into tmp_phenotype_statement (phenos_Pk_id,asubterm_ont_id, asubterm_name,asuperterm_ont_id, asuperterm_name,
bsubterm_ont_id, bsubterm_name,bsuperterm_ont_id, bsuperterm_name, quality_id, quality_name, a_ontology_name,b_ontology_name,quality_tag)
  select phenos_Pk_id, asubterm.term_ont_id, asubterm.term_name, asuperterm.term_ont_id, asuperterm.term_name,
         bsubterm.term_ont_id, bsubterm.term_name, bsuperterm.term_ont_id, bsuperterm.term_name,
         quality.term_ont_id, quality.term_name,  asubterm.term_ontology, bsubterm.term_ontology, phenos_tag
    from phenotype_statement, OUTER term as asubterm, term as asuperterm, OUTER term as bsubterm, OUTER term as bsuperterm,
         term as quality
    where asubterm.term_zdb_id = phenos_entity_1_subterm_zdb_id AND
          asuperterm.term_zdb_id = phenos_entity_1_superterm_zdb_id AND
          bsubterm.term_zdb_id = phenos_entity_2_subterm_zdb_id AND
          bsuperterm.term_zdb_id = phenos_entity_2_superterm_zdb_id AND
          quality.term_zdb_id = phenos_quality_zdb_id;

create index tmp_phenotype_statement_index
  on tmp_phenotype_statement (phenos_pk_id)
  using btree in idxdbs3 ;


! echo "update a relationship name"
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

! echo "update a relationship ID"
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

! echo "update b relationship name"
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

! echo "update b relationship ID"
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

create temp table tmp_pheno_genes (gene_id varchar(50))
with no log;

insert into tmp_pheno_genes
 select gfrv_gene_zdb_id
   from gene_feature_result_view, fish_annotation_Search
   where gfrv_fas_id = fas_pk_id 
   and fas_gene_count = 1
   and fas_pheno_term_group is not null ;
   
insert into tmp_pheno_genes
 select mrel_mrkr_2_zdb_id
   from marker_relationship, genotype_figure_fast_search
   where mrel_type = "knockdown reagent targets gene"
     and mrel_mrkr_1_zdb_id = gffs_morph_zdb_id;   

create temp table tmp_unique (gene_id varchar(50)) with no log;

insert into tmp_unique
select distinct gene_id from tmp_pheno_genes ;

create unique index gene_id_p on tmp_unique 
  (gene_id) using btree in idxdbs2;

select distinct s.gene_id, zdb_id, ortho_abbrev, organism
  from tmp_unique s, orthologue o
  where o.c_gene_id = s.gene_id
  and organism = 'Human'
into temp tmp_o_with_p;


select zdb_id, gene_id, mrkr_abbrev, ortho_abbrev, organism,
	 a.term_ont_id as a_ont_id,a.term_name as e1superName, a.term_ontology as e1superTermOntology,
  	 b.term_ont_id as b_ont_id,b.term_name as e1subName, 
	 b.term_ontology as e1subTermOntology, c.term_ont_id as c_ont_id,
	 c.term_name as e2supername, c.term_ontology as e2superTermOntology,
	 d.term_ont_id as d_ont_id,d.term_name as e2subname, d.term_ontology as e2subTermOntology,
	 e.term_ont_id as e_ont_id, e.term_name as qualityName, phenos_tag,
	 '' as a_relationship_id,'' as a_relationship_name,'' as b_relationship_id,'' as b_relationship_name, phenos_pk_id as phenos_id
    from tmp_o_with_p, feature_marker_relationship, genotype_feature, fish_experiment, fish,
    	 phenotype_experiment, phenotype_statement, marker,
	 term a, 
	 outer term b, 
	 outer term c,
	 outer term d,
	 outer term e,
	 mutant_fast_search
    where fmrel_mrkr_zdb_id = gene_id
    and fmrel_mrkr_Zdb_id = mrkr_zdb_id
    and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
    and genofeat_geno_zdb_id = fish_genotype_zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and phenox_genox_zdb_id = genox_zdb_id
    and phenox_pk_id = phenos_phenox_pk_id
    and a.term_zdb_id = phenos_entity_1_superterm_zdb_id
    and b.term_zdb_id = phenos_entity_1_subterm_zdb_id
    and c.term_zdb_id = phenos_entity_2_superterm_zdb_id
    and d.term_zdb_id = phenos_entity_2_subterm_zdb_id
    and e.term_zdb_id = phenos_quality_zdb_id
    and fmrel_type = 'is allele of'
    and genox_zdb_id = mfs_genox_zdb_id
    and mfs_mrkr_zdb_id = mrkr_zdb_id
union
  select zdb_id, gene_id, mrkr_abbrev, ortho_abbrev, organism,
	 a.term_ont_id as a_ont_id,a.term_name as e1superName, a.term_ontology as e1superTermOntology,
  	 b.term_ont_id as b_ont_id,b.term_name as e1subName, 
	 b.term_ontology as e1subTermOntology, c.term_ont_id as c_ont_id,
	 c.term_name as e2supername, c.term_ontology as e2superTermOntology,
	 d.term_ont_id as d_ont_id,d.term_name as e2subname, d.term_ontology as e2subTermOntology,
	 e.term_ont_id as e_ont_id, e.term_name as qualityName, phenos_tag,
	 '' as a_relationship_id,'' as a_relationship_name,'' as b_relationship_id,'' as b_relationship_name, phenos_pk_id as phenos_id
    from tmp_o_with_p, phenotype_statement, marker, marker_relationship,
	 term a, 
	 outer term b, 
	 outer term c,
	 outer term d,
	 outer term e,
	 genotype_figure_fast_search
    where mrel_mrkr_2_zdb_id = gene_id
    and mrel_type = "knockdown reagent targets gene"
    and mrel_mrkr_2_zdb_id = mrkr_zdb_id
    and mrel_mrkr_1_zdb_id = gffs_morph_zdb_id
    and gffs_phenox_pk_id = phenos_phenox_pk_id
    and a.term_zdb_id = phenos_entity_1_superterm_zdb_id
    and b.term_zdb_id = phenos_entity_1_subterm_zdb_id
    and c.term_zdb_id = phenos_entity_2_superterm_zdb_id
    and d.term_zdb_id = phenos_entity_2_subterm_zdb_id
    and e.term_zdb_id = phenos_quality_zdb_id
union
  select zdb_id, gene_id,  mrkr_abbrev, ortho_abbrev, organism,
	 a.term_ont_id as a_ont_id,a.term_name as e1superName, a.term_ontology as e1superTermOntology,
  	 b.term_ont_id as b_ont_id,b.term_name as e1subName, 
	 b.term_ontology as e1subTermOntology, c.term_ont_id as c_ont_id,
	 c.term_name as e2supername, c.term_ontology as e2superTermOntology,
	 d.term_ont_id as d_ont_id,d.term_name as e2subname, d.term_ontology as e2subTermOntology,
	 e.term_ont_id as e_ont_id, e.term_name as qualityName, phenos_tag,
	 '' as a_relationship_id,'' as a_relationship_name,'' as b_relationship_id,'' as b_relationship_name, phenos_pk_id as phenos_id
    from tmp_o_with_p, marker_relationship, experiment_condition, fish_experiment,
    	 phenotype_Experiment, phenotype_statement, marker,
	 term a, 
	 outer term b, 
	 outer term c,
	 outer term d,
	 outer term e,
	 mutant_fast_search
    where mrel_mrkr_2_zdb_id = gene_id
    and mrkr_zdb_id = mrel_mrkr_2_zdb_id
    and mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id
    and expcond_exp_zdb_id = genox_exp_Zdb_id
    and genox_zdb_id = phenox_genox_zdb_id
    and phenox_pk_id = phenos_phenox_pk_id
   and a.term_Zdb_id = phenos_entity_1_superterm_zdb_id
    and b.term_zdb_id = phenos_entity_1_subterm_zdb_id
    and c.term_zdb_id = phenos_entity_2_superterm_zdb_id
    and d.term_zdb_id = phenos_entity_2_subterm_zdb_id
    and e.term_zdb_id = phenos_quality_zdb_id
    and mfs_genox_zdb_id = genox_zdb_id
    and mfs_mrkr_zdb_id = mrkr_zdb_id
and mfs_mrkr_zdb_id like 'ZDB-GENE%'
into temp tmp_ortho_pheno;

create index tmp_ortho_pheno_index
  on tmp_ortho_pheno (phenos_id)
  using btree in idxdbs3 ;

UNLOAD to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/test_preprocessed_pheno.txt'
select distinct gene_id,  mrkr_abbrev from tmp_ortho_pheno order by gene_id;

UNLOAD to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/preprocessed_pheno.txt'
  select distinct zdb_id, gene_id, mrkr_abbrev,a_ont_id,e1superName, b_ont_id,e1subName, 
  	 c_ont_id,e2superName,d_ont_id,e2subName,e_ont_id,qualityName, phenos_tag,
  	 arelationship_id, arelationship_name, brelationship_id, brelationship_name,quality_id
    from tmp_ortho_pheno, tmp_phenotype_statement
    where phenos_id = phenos_pk_id
    order by gene_id, mrkr_abbrev;


UNLOAD to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/preprocessed_ortho.txt'
  select distinct zdb_id, gene_id, mrkr_abbrev, ortho_abbrev
     from tmp_ortho_pheno
    order by gene_id, mrkr_abbrev, ortho_abbrev;

commit work;
--rollback work ;
