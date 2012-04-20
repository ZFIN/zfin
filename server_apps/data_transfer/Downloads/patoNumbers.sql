begin work;

create temp table tmp_ortho_genes (gene_id varchar(50))
 with no log;

insert into tmp_ortho_genes
  select distinct c_Gene_id from orthologue 
where organism = 'Human';

create index gene_id_o
  on tmp_ortho_genes (gene_id)
  using btree in idxdbs3;

create temp table tmp_pheno_genes (gene_id varchar(50))
with no log;

insert into tmp_pheno_genes
 select gfrv_gene_zdb_id
   from gene_feature_result_view, fish_annotation_Search
   where gfrv_fas_id = fas_pk_id 
   and fas_gene_count = 1
   and fas_pheno_term_group is not null ;


select first 1 * from tmp_pheno_genes;

create temp table tmp_unique (gene_id varchar(50)) with no log;

insert into tmp_unique
select distinct gene_id from tmp_pheno_Genes ;

create unique index gene_id_p on tmp_unique 
  (gene_id) using btree in idxdbs2;

select distinct s.gene_id, zdb_id, ortho_abbrev, organism
  from tmp_unique s, orthologue o
  where o.c_gene_id = s.gene_id
  and organism = 'Human'
into temp tmp_o_with_p;

select count(*) from tmp_o_with_p where gene_id like 'ZDB-ORTH%';

unload to uniqueGeneWithPheno.txt
select tmp_o_with_p.*, mrkr_abbrev
   from tmp_o_with_p, marker
   where mrkr_zdb_id = gene_id;

create temp table tmp_gene_genotype_ortho (gene_id varchar(50), ortho_abbrev varchar(20), 
organism varchar(10), e1SuperTerm_zdb_id varchar(50), e1Subterm_zdb_id varchar(50), 
e2Superterm_zdb_id varchar(50), e2Subterm_zdb_id varchar(20), quality_zdb_id varchar(50), 
tag varchar(20))
with no log;

  select zdb_id, gene_id,  mrkr_abbrev, ortho_abbrev, organism, "morphantPhenotype" as type,mrel_mrkr_1_zdb_id, mrel_mrkr_2_zdb_id, expcond_mrkr_Zdb_id,genox_Zdb_id,
	 a.term_ont_id as a_ont_id,a.term_name as e1superName, a.term_ontology as e1superTermOntology,
  	 b.term_ont_id as b_ont_id,b.term_name as e1subName, 
	 b.term_ontology as e1subTermOntology, c.term_ont_id as c_ont_id,
	 c.term_name as e2supername, c.term_ontology as e2superTermOntology,
	 d.term_ont_id as d_ont_id,d.term_name as e2subname, d.term_ontology as e2subTermOntology,
	 e.term_ont_id as e_ont_id, e.term_name as qualityName, phenos_tag, "              " as entrezZebrafishId, "              " as entrezHumanId
    from tmp_o_with_p, marker_relationship, experiment_condition, genotype_experiment, 
    	 phenotype_Experiment, phenotype_statement, marker,
	 term a, 
	 outer term b, 
	 outer term c,
	 outer term d,
	 outer term e,
	 mutant_fast_search,
	 fish_Annotation_Search,
	 gene_feature_result_view,
	 figure_term_fish_search
    where mrel_mrkr_2_zdb_id = gene_id
    and mrkr_zdb_id = mrel_mrkr_2_zdb_id
    and mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id
    and expcond_exp_zdb_id = genox_exp_Zdb_id
    and genox_zdb_id = phenox_genox_Zdb_id
    and phenox_pk_id = phenos_phenox_pk_id
   and a.term_Zdb_id = phenos_entity_1_superterm_zdb_id
    and b.term_zdb_id = phenos_entity_1_subterm_zdb_id
    and c.term_zdb_id = phenos_entity_2_superterm_zdb_id
    and d.term_zdb_id = phenos_entity_2_subterm_zdb_id
    and e.term_zdb_id = phenos_quality_zdb_id
    and mfs_genox_zdb_id = genox_Zdb_id
    and mfs_mrkr_zdb_id = mrkr_Zdb_id
    and mfs_mrkr_zdb_id like 'ZDB-GENE%'
and mrkr_abbrev = 'sox17'
 and a.term_name = 'forerunner cell group'
 and ftfs_fas_id = fas_pk_id
and gfrv_fas_id = fas_pk_id
    and fas_pheno_term_group is not null
    and fas_gene_count = 1
    and ftfs_genox_zdb_id = genox_zdb_id;


  select zdb_id, gene_id, mrkr_abbrev, ortho_abbrev, organism, "featurePhenotype" as type,
	 a.term_ont_id as a_ont_id,a.term_name as e1superName, a.term_ontology as e1superTermOntology,
  	 b.term_ont_id as b_ont_id,b.term_name as e1subName, 
	 b.term_ontology as e1subTermOntology, c.term_ont_id as c_ont_id,
	 c.term_name as e2supername, c.term_ontology as e2superTermOntology,
	 d.term_ont_id as d_ont_id,d.term_name as e2subname, d.term_ontology as e2subTermOntology,
	 e.term_ont_id as e_ont_id, e.term_name as qualityName, phenos_tag, "" as entrezZebrafishId, "" as entrezHumanId
    from tmp_o_with_p, feature_marker_relationship, genotype_feature, genotype_experiment, 
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
    and genofeat_geno_zdb_id = genox_geno_zdb_id
    and phenox_genox_zdb_id = genox_zdb_id
    and phenox_pk_id = phenos_phenox_pk_id
    and a.term_Zdb_id = phenos_entity_1_superterm_zdb_id
    and b.term_zdb_id = phenos_entity_1_subterm_zdb_id
    and c.term_zdb_id = phenos_entity_2_superterm_zdb_id
    and d.term_zdb_id = phenos_entity_2_subterm_zdb_id
    and e.term_zdb_id = phenos_quality_zdb_id
    and fmrel_type = 'is allele of'
    and genox_zdb_id = mfs_genox_Zdb_id
    and mfs_mrkr_zdb_id = mrkr_Zdb_id
union
  select zdb_id, gene_id,  mrkr_abbrev, ortho_abbrev, organism, "morphantPhenotype" as type,
	 a.term_ont_id as a_ont_id,a.term_name as e1superName, a.term_ontology as e1superTermOntology,
  	 b.term_ont_id as b_ont_id,b.term_name as e1subName, 
	 b.term_ontology as e1subTermOntology, c.term_ont_id as c_ont_id,
	 c.term_name as e2supername, c.term_ontology as e2superTermOntology,
	 d.term_ont_id as d_ont_id,d.term_name as e2subname, d.term_ontology as e2subTermOntology,
	 e.term_ont_id as e_ont_id, e.term_name as qualityName, phenos_tag, "              " as entrezZebrafishId, "              " as entrezHumanId
    from tmp_o_with_p, marker_relationship, experiment_condition, genotype_experiment, 
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
    and genox_zdb_id = phenox_genox_Zdb_id
    and phenox_pk_id = phenos_phenox_pk_id
   and a.term_Zdb_id = phenos_entity_1_superterm_zdb_id
    and b.term_zdb_id = phenos_entity_1_subterm_zdb_id
    and c.term_zdb_id = phenos_entity_2_superterm_zdb_id
    and d.term_zdb_id = phenos_entity_2_subterm_zdb_id
    and e.term_zdb_id = phenos_quality_zdb_id
    and mfs_genox_zdb_id = genox_Zdb_id
    and mfs_mrkr_zdb_id = mrkr_Zdb_id
and mfs_mrkr_zdb_id like 'ZDB-GENE%'
into temp tmp_ortho_pheno;

!echo "tp53 count";
select count(*) from tmp_ortho_pheno
 where mrkr_abbrev = 'tp53';

!echo "dld count"
 select count(*) from tmp_ortho_pheno
 where mrkr_abbrev = 'dld';

select count(*) as counter, dblink_Acc_num, dblink_linked_recid
 from db_link, tmp_ortho_pheno
 where gene_id = dblink_linked_recid
 and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
 group by dblink_acc_num, dblink_linked_recid having count(*) > 1
  into temp tmp_dups;

select count(*), dblink_linked_recid
  from tmp_dups
  group by dblink_linked_recid
 having count(*) > 1;

update tmp_ortho_pheno
  set entrezZebrafishId = (select distinct dblink_acc_num from db_link
      			  	  where dblink_linked_recid = gene_id
				  and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1')
  where gene_id not in ('ZDB-GENE-071130-1')   ;

select count(*) from tmp_ortho_pheno
 where entrezZebrafishId is null;

update tmp_ortho_pheno
  set entrezHumanId = (select distinct dblink_acc_num from db_link
      			  	  where dblink_linked_recid = zdb_id
				  and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-27');

--select distinct count(*) 
 -- from tmp_ortho_pheno;

unload to pheno.txt
  select gene_id, entrezZebrafishId,entrezHumanId, mrkr_abbrev,a_ont_id,e1superName, b_ont_id,e1subName, 
  	 c_ont_id,e2superName,d_ont_id,e2subName,e_ont_id,qualityName, phenos_tag  from tmp_ortho_pheno
    order by gene_id, mrkr_abbrev, ortho_abbrev, type;

unload to ortho_gene.txt
  select distinct gene_id, mrkr_abbrev, entrezZebrafishId, ortho_abbrev, entrezHumanId
     from tmp_ortho_pheno
    order by gene_id, mrkr_abbrev, ortho_abbrev;

select distinct mrkr_abbrev from tmp_ortho_pheno where entrezZebrafishId is null;
select distinct mrkr_abbrev from tmp_ortho_pheno where entrezHumanId is null;


--select a.gene_id from tmp_ortho_pheno a
--  where not exists (Select 'x' from tmp_o_with_p
--  	    	   	   where tmp_o_with_p.gene_id = a.gene_id);

--select a.gene_id from tmp_o_with_p a
--  where not exists (Select 'x' from tmp_ortho_pheno
--  	    	   	   where tmp_ortho_pheno.gene_id = a.gene_id);

unload to testPheno.txt
    select * from tmp_ortho_pheno
    where mrkr_abbrev = 'brpf1'
    order by e1superName, e1subName, e2superName, e2subName;

--commit work;

rollback work ;