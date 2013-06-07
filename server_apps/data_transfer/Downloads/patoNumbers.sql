begin work;

create temp table tmp_pheno_genes (gene_id varchar(50))
with no log;

insert into tmp_pheno_genes
 select gfrv_gene_zdb_id
   from gene_feature_result_view, fish_annotation_Search
   where gfrv_fas_id = fas_pk_id 
   and fas_gene_count = 1
   and fas_pheno_term_group is not null ;


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


select zdb_id, gene_id, mrkr_abbrev, ortho_abbrev, organism,
	 a.term_ont_id as a_ont_id,a.term_name as e1superName, a.term_ontology as e1superTermOntology,
  	 b.term_ont_id as b_ont_id,b.term_name as e1subName, 
	 b.term_ontology as e1subTermOntology, c.term_ont_id as c_ont_id,
	 c.term_name as e2supername, c.term_ontology as e2superTermOntology,
	 d.term_ont_id as d_ont_id,d.term_name as e2subname, d.term_ontology as e2subTermOntology,
	 e.term_ont_id as e_ont_id, e.term_name as qualityName, phenos_tag
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
  select zdb_id, gene_id,  mrkr_abbrev, ortho_abbrev, organism,
	 a.term_ont_id as a_ont_id,a.term_name as e1superName, a.term_ontology as e1superTermOntology,
  	 b.term_ont_id as b_ont_id,b.term_name as e1subName, 
	 b.term_ontology as e1subTermOntology, c.term_ont_id as c_ont_id,
	 c.term_name as e2supername, c.term_ontology as e2superTermOntology,
	 d.term_ont_id as d_ont_id,d.term_name as e2subname, d.term_ontology as e2subTermOntology,
	 e.term_ont_id as e_ont_id, e.term_name as qualityName, phenos_tag
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

UNLOAD to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/preprocessed_pheno.txt'
  select distinct zdb_id, gene_id, mrkr_abbrev,a_ont_id,e1superName, b_ont_id,e1subName, 
  	 c_ont_id,e2superName,d_ont_id,e2subName,e_ont_id,qualityName, phenos_tag  
    from tmp_ortho_pheno
    order by gene_id, mrkr_abbrev;


UNLOAD to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/preprocessed_ortho.txt'
  select distinct zdb_id, gene_id, mrkr_abbrev, ortho_abbrev
     from tmp_ortho_pheno
    order by gene_id, mrkr_abbrev, ortho_abbrev;


unload to testPheno.txt
    select * from tmp_ortho_pheno
    where mrkr_abbrev = 'brpf1'
    order by e1superName, e1subName, e2superName, e2subName;

commit work;
--rollback work ;
