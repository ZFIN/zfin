begin work;

-- runNumbersFBcase8787.sql
-- created by Sierra; modified by Xiang
-- for FB case 8787, Phenoscape Paired fin data request

-- qualities
-- MO gene only

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


---MORPHS
create temp table tmp_pheno_gene (id varchar(50), genox_zdb_id varchar(50), gene_abbrev varchar(50), gene_zdb_id varchar(50), term_ont_id varchar(30), term_name varchar(100), whereFrom varchar(20), geno_id varchar(50), mo_id varchar(50), stage_start_id varchar(50), stage_end_id varchar(50))
 with no log;

insert into tmp_pheno_gene
select distinct phenos_pk_id, genox_Zdb_id, mrkr_abbrev, mrkr_zdb_id, b.term_ont_id, b.term_name, 'pheno', fish_genotype_zdb_id, expcond_mrkr_zdb_id, phenox_start_stg_zdb_id, phenox_end_stg_zdb_id
  from all_term_contains, 
       fish_experiment, mutant_Fast_search, phenotype_statement, phenotype_experiment, term a, marker, term b, experiment, experiment_condition, fish
  where mfs_genox_zdb_id = genox_zdb_id	
  and mfs_mrkr_Zdb_id = mrkr_zdb_id
  and mrkr_zdb_id like 'ZDB-GENE%'
  and expcond_exp_zdb_id =exp_zdb_id
  and exp_zdb_id = genox_exp_zdb_id
  and fish_zdb_id = genox_fish_zdb_id
  and alltermcon_contained_zdb_id = phenos_entity_2_subterm_zdb_id
  and a.term_zdb_id = alltermcon_container_zdb_id
  and a.term_name in ('fin bud',
 'pelvic fin field' ,
 'pectoral fin field', 
 'paired fin skeleton', 
 'apical ectodermal ridge pectoral fin bud' ,
 'pectoral fin bud' ,
 'cleithrum' ,
 'mesocoracoid bone', 
 'pectoral fin cartilage', 
 'pelvic fin musculature' ,
 'superficial adductor' ,
 'coracoid' ,
 'pectoral girdle', 
 'postcleithrum' ,
 'superficial pelvic abductor', 
 'coracoradialis' ,
 'pelvic adductor profundus' ,
 'dorsal arrector' ,
 'pelvic radial' ,
 'posttemporal' ,
 'pectoral fin musculature' ,
 'pelvic abductor profundus' ,
 'pelvic girdle' ,
 'scapula' ,
 'superficial pelvic adductor' ,
 'supracleithrum' ,
 'abductor profundus', 
 'adductor profundus' ,
 'basipterygium' ,
 'dorsal pelvic arrector' ,
 'extrascapula' ,
 'superficial abductor' ,
 'ventral arrector' ,
 'ventral pelvic arrector' ,
 'mesoderm pectoral fin bud' ,
 'pectoral fin skeleton' ,
 'mesenchyme pectoral fin', 
 'pectoral fin' ,
 'pelvic fin' ,
 'pelvic fin bud',
 'apic1al ectodermal ridge pelvic fin bud' ,
 'mesoderm pelvic fin bud' ,
 'pelvic fin skeleton' ,
 'pelvic radial 2' ,
 'pelvic radial 3' ,
 'pelvic radial 1' ,
 'mesenchyme pelvic fin' ,
 'apical ectodermal ridge pelvic fin' ,
 'zone of polarizing activity pectoral fin bud' ,
 'zone of polarizing activity pelvic fin bud' ,
 'scapulocoracoid' ,
 'pectoral fin endoskeletal disc' ,
 'pelvic fin cartilage' ,
 'mesocoracoid cartilage' ,
 'pelvic radial cartilage' ,
 'basipterygium cartilage' ,
 'pelvic radial 3 cartilage', 
 'pelvic radial 2 cartilage' ,
 'pelvic radial 1 cartilage' ,
 'paired fin cartilage' ,
 'pectoral fin lepidotrichium', 
 'pelvic fin lepidotrichium' ,
 'pectoral fin radial',
 'pectoral fin proximal radial' ,
 'pectoral fin distal radial' ,
 'propterygium' ,
 'pectoral artery' ,
 'pectoral vein' ,
 'pectoral fin vasculature' ,
 'pectoral fin blood vessel',
 'pectoral fin lymph vessel',
 'fin fold pectoral fin bud',
 'pectoral fin fold',
 'pectoral fin nerve', 
 'pectoral fin motor nerve' ,
 'pectoral fin sensory nerve' ,
 'pectoral fin motor nerve 1' ,
 'pectoral fin motor nerve 2' ,
 'pectoral fin motor nerve 3' ,
 'pectoral fin motor nerve 4' ,
 'pectoral fin actinotrichium' ,
 'pelvic fin actinotrichium' ,
 'pectoral fin lepidotrichium 1' ,
 'pectoral fin lepidotrichium 2' ,
 'pectoral fin lepidotrichium 3' ,
 'pectoral fin lepidotrichium 6' ,
 'pectoral fin lepidotrichium 7' ,
 'pectoral fin lepidotrichium 5' ,
 'pectoral fin lepidotrichium 4' ,
 'pelvic fin lepidotrichium 1' ,
 'pelvic fin lepidotrichium 2' ,
 'pelvic fin lepidotrichium 4',
 'pelvic fin lepidotrichium 3',
 'paired fin',
 'primitive pectoral fin adductor',
 'pectoral fin development' ,
'pelvic fin development',
 'pectoral fin morphogenesis',
 'embryonic pectoral fin morphogenesis',
 'post-embryonic pectoral fin morphogenesis',
    'pelvic fin morphogenesis',
 'embryonic pelvic fin morphogenesis',
 'post-embryonic pelvic fin morphogenesis'
 )
  and phenox_genox_zdb_id = genox_zdb_id
  and phenos_phenox_pk_id = phenox_pk_id
  and phenos_tag != 'normal'
  and b.term_zdb_id = alltermcon_contained_zdb_id
 ;


insert into tmp_pheno_gene
select distinct phenos_pk_id, genox_Zdb_id,mrkr_abbrev, mrkr_zdb_id, b.term_ont_id, b.term_name, 'pheno', fish_genotype_zdb_id, expcond_mrkr_zdb_id, phenox_start_stg_zdb_id, phenox_end_stg_zdb_id
  from all_term_contains, 
       fish_experiment, mutant_Fast_search, phenotype_statement, phenotype_experiment, term a, marker, term b, experiment, experiment_condition, fish
  where mfs_genox_zdb_id = genox_zdb_id	
  and mfs_mrkr_Zdb_id = mrkr_zdb_id
and mrkr_zdb_id like 'ZDB-GENE%'
  and expcond_exp_zdb_id =exp_zdb_id
  and exp_zdb_id = genox_exp_zdb_id
  and fish_zdb_id = genox_fish_zdb_id
  and alltermcon_contained_zdb_id = phenos_entity_2_superterm_zdb_id
  and a.term_zdb_id = alltermcon_container_zdb_id
  and a.term_name in ('fin bud',
 'pelvic fin field' ,
 'pectoral fin field', 
 'paired fin skeleton', 
 'apical ectodermal ridge pectoral fin bud' ,
 'pectoral fin bud' ,
 'cleithrum' ,
 'mesocoracoid bone', 
 'pectoral fin cartilage', 
 'pelvic fin musculature' ,
 'superficial adductor' ,
 'coracoid' ,
 'pectoral girdle', 
 'postcleithrum' ,
 'superficial pelvic abductor', 
 'coracoradialis' ,
 'pelvic adductor profundus' ,
 'dorsal arrector' ,
 'pelvic radial' ,
 'posttemporal' ,
 'pectoral fin musculature' ,
 'pelvic abductor profundus' ,
 'pelvic girdle' ,
 'scapula' ,
 'superficial pelvic adductor' ,
 'supracleithrum' ,
 'abductor profundus', 
 'adductor profundus' ,
 'basipterygium' ,
 'dorsal pelvic arrector' ,
 'extrascapula' ,
 'superficial abductor' ,
 'ventral arrector' ,
 'ventral pelvic arrector' ,
 'mesoderm pectoral fin bud' ,
 'pectoral fin skeleton' ,
 'mesenchyme pectoral fin', 
 'pectoral fin' ,
 'pelvic fin' ,
 'pelvic fin bud',
 'apic1al ectodermal ridge pelvic fin bud' ,
 'mesoderm pelvic fin bud' ,
 'pelvic fin skeleton' ,
 'pelvic radial 2' ,
 'pelvic radial 3' ,
 'pelvic radial 1' ,
 'mesenchyme pelvic fin' ,
 'apical ectodermal ridge pelvic fin' ,
 'zone of polarizing activity pectoral fin bud' ,
 'zone of polarizing activity pelvic fin bud' ,
 'scapulocoracoid' ,
 'pectoral fin endoskeletal disc' ,
 'pelvic fin cartilage' ,
 'mesocoracoid cartilage' ,
 'pelvic radial cartilage' ,
 'basipterygium cartilage' ,
 'pelvic radial 3 cartilage', 
 'pelvic radial 2 cartilage' ,
 'pelvic radial 1 cartilage' ,
 'paired fin cartilage' ,
 'pectoral fin lepidotrichium', 
 'pelvic fin lepidotrichium' ,
 'pectoral fin radial',
 'pectoral fin proximal radial' ,
 'pectoral fin distal radial' ,
 'propterygium' ,
 'pectoral artery' ,
 'pectoral vein' ,
 'pectoral fin vasculature' ,
 'pectoral fin blood vessel',
 'pectoral fin lymph vessel',
 'fin fold pectoral fin bud',
 'pectoral fin fold',
 'pectoral fin nerve', 
 'pectoral fin motor nerve' ,
 'pectoral fin sensory nerve' ,
 'pectoral fin motor nerve 1' ,
 'pectoral fin motor nerve 2' ,
 'pectoral fin motor nerve 3' ,
 'pectoral fin motor nerve 4' ,
 'pectoral fin actinotrichium' ,
 'pelvic fin actinotrichium' ,
 'pectoral fin lepidotrichium 1' ,
 'pectoral fin lepidotrichium 2' ,
 'pectoral fin lepidotrichium 3' ,
 'pectoral fin lepidotrichium 6' ,
 'pectoral fin lepidotrichium 7' ,
 'pectoral fin lepidotrichium 5' ,
 'pectoral fin lepidotrichium 4' ,
 'pelvic fin lepidotrichium 1' ,
 'pelvic fin lepidotrichium 2' ,
 'pelvic fin lepidotrichium 4',
 'pelvic fin lepidotrichium 3',
 'paired fin',
 'primitive pectoral fin adductor',
 'pectoral fin development' ,
'pelvic fin development',
 'pectoral fin morphogenesis',
 'embryonic pectoral fin morphogenesis',
 'post-embryonic pectoral fin morphogenesis',
    'pelvic fin morphogenesis',
 'embryonic pelvic fin morphogenesis',
 'post-embryonic pelvic fin morphogenesis'
 )
  and phenox_genox_zdb_id = genox_zdb_id
  and phenos_phenox_pk_id = phenox_pk_id
  and phenos_tag != 'normal'
  and b.term_zdb_id = alltermcon_contained_zdb_id
 ;


insert into tmp_pheno_gene
select distinct phenos_pk_id, genox_Zdb_id,mrkr_abbrev, mrkr_zdb_id, b.term_ont_id, b.term_name, 'pheno', fish_genotype_zdb_id, expcond_mrkr_zdb_id, phenox_start_stg_zdb_id, phenox_end_stg_zdb_id
  from all_term_contains, 
       fish_experiment, mutant_Fast_search, phenotype_statement, phenotype_experiment, term a, marker, term b, experiment, experiment_condition, fish
  where mfs_genox_zdb_id = genox_zdb_id	
  and mfs_mrkr_Zdb_id = mrkr_zdb_id
and mrkr_zdb_id like 'ZDB-GENE%'
  and expcond_exp_zdb_id =exp_zdb_id
  and exp_zdb_id = genox_exp_zdb_id
  and fish_zdb_id = genox_fish_zdb_id
  and alltermcon_contained_zdb_id = phenos_entity_1_subterm_zdb_id
  and a.term_zdb_id = alltermcon_container_zdb_id
  and a.term_name in ('fin bud',
 'pelvic fin field' ,
 'pectoral fin field', 
 'paired fin skeleton', 
 'apical ectodermal ridge pectoral fin bud' ,
 'pectoral fin bud' ,
 'cleithrum' ,
 'mesocoracoid bone', 
 'pectoral fin cartilage', 
 'pelvic fin musculature' ,
 'superficial adductor' ,
 'coracoid' ,
 'pectoral girdle', 
 'postcleithrum' ,
 'superficial pelvic abductor', 
 'coracoradialis' ,
 'pelvic adductor profundus' ,
 'dorsal arrector' ,
 'pelvic radial' ,
 'posttemporal' ,
 'pectoral fin musculature' ,
 'pelvic abductor profundus' ,
 'pelvic girdle' ,
 'scapula' ,
 'superficial pelvic adductor' ,
 'supracleithrum' ,
 'abductor profundus', 
 'adductor profundus' ,
 'basipterygium' ,
 'dorsal pelvic arrector' ,
 'extrascapula' ,
 'superficial abductor' ,
 'ventral arrector' ,
 'ventral pelvic arrector' ,
 'mesoderm pectoral fin bud' ,
 'pectoral fin skeleton' ,
 'mesenchyme pectoral fin', 
 'pectoral fin' ,
 'pelvic fin' ,
 'pelvic fin bud',
 'apic1al ectodermal ridge pelvic fin bud' ,
 'mesoderm pelvic fin bud' ,
 'pelvic fin skeleton' ,
 'pelvic radial 2' ,
 'pelvic radial 3' ,
 'pelvic radial 1' ,
 'mesenchyme pelvic fin' ,
 'apical ectodermal ridge pelvic fin' ,
 'zone of polarizing activity pectoral fin bud' ,
 'zone of polarizing activity pelvic fin bud' ,
 'scapulocoracoid' ,
 'pectoral fin endoskeletal disc' ,
 'pelvic fin cartilage' ,
 'mesocoracoid cartilage' ,
 'pelvic radial cartilage' ,
 'basipterygium cartilage' ,
 'pelvic radial 3 cartilage', 
 'pelvic radial 2 cartilage' ,
 'pelvic radial 1 cartilage' ,
 'paired fin cartilage' ,
 'pectoral fin lepidotrichium', 
 'pelvic fin lepidotrichium' ,
 'pectoral fin radial',
 'pectoral fin proximal radial' ,
 'pectoral fin distal radial' ,
 'propterygium' ,
 'pectoral artery' ,
 'pectoral vein' ,
 'pectoral fin vasculature' ,
 'pectoral fin blood vessel',
 'pectoral fin lymph vessel',
 'fin fold pectoral fin bud',
 'pectoral fin fold',
 'pectoral fin nerve', 
 'pectoral fin motor nerve' ,
 'pectoral fin sensory nerve' ,
 'pectoral fin motor nerve 1' ,
 'pectoral fin motor nerve 2' ,
 'pectoral fin motor nerve 3' ,
 'pectoral fin motor nerve 4' ,
 'pectoral fin actinotrichium' ,
 'pelvic fin actinotrichium' ,
 'pectoral fin lepidotrichium 1' ,
 'pectoral fin lepidotrichium 2' ,
 'pectoral fin lepidotrichium 3' ,
 'pectoral fin lepidotrichium 6' ,
 'pectoral fin lepidotrichium 7' ,
 'pectoral fin lepidotrichium 5' ,
 'pectoral fin lepidotrichium 4' ,
 'pelvic fin lepidotrichium 1' ,
 'pelvic fin lepidotrichium 2' ,
 'pelvic fin lepidotrichium 4',
 'pelvic fin lepidotrichium 3',
 'paired fin',
 'primitive pectoral fin adductor',
 'pectoral fin development' ,
'pelvic fin development',
 'pectoral fin morphogenesis',
 'embryonic pectoral fin morphogenesis',
 'post-embryonic pectoral fin morphogenesis',
    'pelvic fin morphogenesis',
 'embryonic pelvic fin morphogenesis',
 'post-embryonic pelvic fin morphogenesis'
 )
  and phenox_genox_zdb_id = genox_zdb_id
  and phenos_phenox_pk_id = phenox_pk_id
  and phenos_tag != 'normal'
  and b.term_zdb_id = alltermcon_contained_zdb_id
 ;


insert into tmp_pheno_gene
select distinct phenos_pk_id, genox_Zdb_id,mrkr_abbrev, mrkr_zdb_id, b.term_ont_id, b.term_name, 'pheno', fish_genotype_zdb_id, expcond_mrkr_zdb_id, phenox_start_stg_zdb_id, phenox_end_stg_zdb_id
  from all_term_contains, 
       fish_experiment, mutant_Fast_search, phenotype_statement, phenotype_experiment, term a, marker, term b, experiment, experiment_condition, fish
  where mfs_genox_zdb_id = genox_zdb_id	
  and mfs_mrkr_Zdb_id = mrkr_zdb_id
  and expcond_exp_zdb_id =exp_zdb_id
  and fish_zdb_id = genox_fish_zdb_id
  and mrkr_zdb_id like 'ZDB-GENE%'
  and exp_zdb_id = genox_exp_zdb_id
  and alltermcon_contained_zdb_id = phenos_entity_1_superterm_zdb_id
  and a.term_zdb_id = alltermcon_container_zdb_id
  and a.term_name in ('fin bud',
 'pelvic fin field' ,
 'pectoral fin field', 
 'paired fin skeleton', 
 'apical ectodermal ridge pectoral fin bud' ,
 'pectoral fin bud' ,
 'cleithrum' ,
 'mesocoracoid bone', 
 'pectoral fin cartilage', 
 'pelvic fin musculature' ,
 'superficial adductor' ,
 'coracoid' ,
 'pectoral girdle', 
 'postcleithrum' ,
 'superficial pelvic abductor', 
 'coracoradialis' ,
 'pelvic adductor profundus' ,
 'dorsal arrector' ,
 'pelvic radial' ,
 'posttemporal' ,
 'pectoral fin musculature' ,
 'pelvic abductor profundus' ,
 'pelvic girdle' ,
 'scapula' ,
 'superficial pelvic adductor' ,
 'supracleithrum' ,
 'abductor profundus', 
 'adductor profundus' ,
 'basipterygium' ,
 'dorsal pelvic arrector' ,
 'extrascapula' ,
 'superficial abductor' ,
 'ventral arrector' ,
 'ventral pelvic arrector' ,
 'mesoderm pectoral fin bud' ,
 'pectoral fin skeleton' ,
 'mesenchyme pectoral fin', 
 'pectoral fin' ,
 'pelvic fin' ,
 'pelvic fin bud',
 'apic1al ectodermal ridge pelvic fin bud' ,
 'mesoderm pelvic fin bud' ,
 'pelvic fin skeleton' ,
 'pelvic radial 2' ,
 'pelvic radial 3' ,
 'pelvic radial 1' ,
 'mesenchyme pelvic fin' ,
 'apical ectodermal ridge pelvic fin' ,
 'zone of polarizing activity pectoral fin bud' ,
 'zone of polarizing activity pelvic fin bud' ,
 'scapulocoracoid' ,
 'pectoral fin endoskeletal disc' ,
 'pelvic fin cartilage' ,
 'mesocoracoid cartilage' ,
 'pelvic radial cartilage' ,
 'basipterygium cartilage' ,
 'pelvic radial 3 cartilage', 
 'pelvic radial 2 cartilage' ,
 'pelvic radial 1 cartilage' ,
 'paired fin cartilage' ,
 'pectoral fin lepidotrichium', 
 'pelvic fin lepidotrichium' ,
 'pectoral fin radial',
 'pectoral fin proximal radial' ,
 'pectoral fin distal radial' ,
 'propterygium' ,
 'pectoral artery' ,
 'pectoral vein' ,
 'pectoral fin vasculature' ,
 'pectoral fin blood vessel',
 'pectoral fin lymph vessel',
 'fin fold pectoral fin bud',
 'pectoral fin fold',
 'pectoral fin nerve', 
 'pectoral fin motor nerve' ,
 'pectoral fin sensory nerve' ,
 'pectoral fin motor nerve 1' ,
 'pectoral fin motor nerve 2' ,
 'pectoral fin motor nerve 3' ,
 'pectoral fin motor nerve 4' ,
 'pectoral fin actinotrichium' ,
 'pelvic fin actinotrichium' ,
 'pectoral fin lepidotrichium 1' ,
 'pectoral fin lepidotrichium 2' ,
 'pectoral fin lepidotrichium 3' ,
 'pectoral fin lepidotrichium 6' ,
 'pectoral fin lepidotrichium 7' ,
 'pectoral fin lepidotrichium 5' ,
 'pectoral fin lepidotrichium 4' ,
 'pelvic fin lepidotrichium 1' ,
 'pelvic fin lepidotrichium 2' ,
 'pelvic fin lepidotrichium 4',
 'pelvic fin lepidotrichium 3',
 'paired fin',
 'primitive pectoral fin adductor',
 'pectoral fin development' ,
'pelvic fin development',
 'pectoral fin morphogenesis',
 'embryonic pectoral fin morphogenesis',
 'post-embryonic pectoral fin morphogenesis',
    'pelvic fin morphogenesis',
 'embryonic pelvic fin morphogenesis',
 'post-embryonic pelvic fin morphogenesis'
 )
  and phenox_genox_zdb_id = genox_zdb_id
  and phenos_phenox_pk_id = phenox_pk_id
  and phenos_tag != 'normal'
  and b.term_zdb_id = alltermcon_contained_zdb_id
 ;



---ALLELES
insert into tmp_pheno_gene
select distinct phenos_pk_id, genox_Zdb_id,mrkr_abbrev, mrkr_zdb_id, b.term_ont_id, b.term_name, 'pheno', fish_genotype_zdb_id, expcond_mrkr_zdb_id, phenox_start_stg_zdb_id, phenox_end_stg_zdb_id
  from feature_marker_Relationship, genotype_Feature, all_term_contains, 
       fish_experiment, mutant_Fast_search, phenotype_statement, phenotype_experiment, term a, marker, term b, experiment, experiment_condition, fish
  where fmrel_ftr_zdb_id = genofeat_feature_zdb_id
  and fish_genotype_Zdb_id = genofeat_geno_zdb_id
  and mfs_genox_zdb_id = genox_zdb_id	
  and fish_zdb_id = genox_fish_zdb_id
  and mfs_mrkr_Zdb_id = fmrel_mrkr_Zdb_id
  and fmrel_type = 'is allele of'
  and expcond_exp_zdb_id =exp_zdb_id
  and exp_zdb_id = genox_exp_zdb_id
  and alltermcon_contained_zdb_id = phenos_entity_1_superterm_zdb_id
  and a.term_zdb_id = alltermcon_container_zdb_id
  and a.term_name in ('fin bud',
 'pelvic fin field' ,
 'pectoral fin field', 
 'paired fin skeleton', 
 'apical ectodermal ridge pectoral fin bud' ,
 'pectoral fin bud' ,
 'cleithrum' ,
 'mesocoracoid bone', 
 'pectoral fin cartilage', 
 'pelvic fin musculature' ,
 'superficial adductor' ,
 'coracoid' ,
 'pectoral girdle', 
 'postcleithrum' ,
 'superficial pelvic abductor', 
 'coracoradialis' ,
 'pelvic adductor profundus' ,
 'dorsal arrector' ,
 'pelvic radial' ,
 'posttemporal' ,
 'pectoral fin musculature' ,
 'pelvic abductor profundus' ,
 'pelvic girdle' ,
 'scapula' ,
 'superficial pelvic adductor' ,
 'supracleithrum' ,
 'abductor profundus', 
 'adductor profundus' ,
 'basipterygium' ,
 'dorsal pelvic arrector' ,
 'extrascapula' ,
 'superficial abductor' ,
 'ventral arrector' ,
 'ventral pelvic arrector' ,
 'mesoderm pectoral fin bud' ,
 'pectoral fin skeleton' ,
 'mesenchyme pectoral fin', 
 'pectoral fin' ,
 'pelvic fin' ,
 'pelvic fin bud',
 'apic1al ectodermal ridge pelvic fin bud' ,
 'mesoderm pelvic fin bud' ,
 'pelvic fin skeleton' ,
 'pelvic radial 2' ,
 'pelvic radial 3' ,
 'pelvic radial 1' ,
 'mesenchyme pelvic fin' ,
 'apical ectodermal ridge pelvic fin' ,
 'zone of polarizing activity pectoral fin bud' ,
 'zone of polarizing activity pelvic fin bud' ,
 'scapulocoracoid' ,
 'pectoral fin endoskeletal disc' ,
 'pelvic fin cartilage' ,
 'mesocoracoid cartilage' ,
 'pelvic radial cartilage' ,
 'basipterygium cartilage' ,
 'pelvic radial 3 cartilage', 
 'pelvic radial 2 cartilage' ,
 'pelvic radial 1 cartilage' ,
 'paired fin cartilage' ,
 'pectoral fin lepidotrichium', 
 'pelvic fin lepidotrichium' ,
 'pectoral fin radial',
 'pectoral fin proximal radial' ,
 'pectoral fin distal radial' ,
 'propterygium' ,
 'pectoral artery' ,
 'pectoral vein' ,
 'pectoral fin vasculature' ,
 'pectoral fin blood vessel',
 'pectoral fin lymph vessel',
 'fin fold pectoral fin bud',
 'pectoral fin fold',
 'pectoral fin nerve', 
 'pectoral fin motor nerve' ,
 'pectoral fin sensory nerve' ,
 'pectoral fin motor nerve 1' ,
 'pectoral fin motor nerve 2' ,
 'pectoral fin motor nerve 3' ,
 'pectoral fin motor nerve 4' ,
 'pectoral fin actinotrichium' ,
 'pelvic fin actinotrichium' ,
 'pectoral fin lepidotrichium 1' ,
 'pectoral fin lepidotrichium 2' ,
 'pectoral fin lepidotrichium 3' ,
 'pectoral fin lepidotrichium 6' ,
 'pectoral fin lepidotrichium 7' ,
 'pectoral fin lepidotrichium 5' ,
 'pectoral fin lepidotrichium 4' ,
 'pelvic fin lepidotrichium 1' ,
 'pelvic fin lepidotrichium 2' ,
 'pelvic fin lepidotrichium 4',
 'pelvic fin lepidotrichium 3',
 'paired fin',
 'primitive pectoral fin adductor',
 'pectoral fin development' ,
'pelvic fin development',
 'pectoral fin morphogenesis',
 'embryonic pectoral fin morphogenesis',
 'post-embryonic pectoral fin morphogenesis',
    'pelvic fin morphogenesis',
 'embryonic pelvic fin morphogenesis',
 'post-embryonic pelvic fin morphogenesis'
 )
  and phenox_genox_zdb_id = genox_zdb_id
  and phenos_phenox_pk_id = phenox_pk_id
  and phenos_tag != 'normal'
  and mrkr_zdb_id = mfs_mrkr_zdb_id
  and mrkr_zdb_id = fmrel_mrkr_zdb_id
  and b.term_zdb_id = alltermcon_contained_zdb_id
 ;

insert into tmp_pheno_gene
select distinct phenos_pk_id, genox_Zdb_id,mrkr_abbrev, mrkr_zdb_id, b.term_ont_id, b.term_name, 'pheno', fish_genotype_zdb_id, expcond_mrkr_zdb_id, phenox_start_stg_zdb_id, phenox_end_stg_zdb_id
  from feature_marker_Relationship, genotype_Feature, all_term_contains, 
       fish_experiment, mutant_Fast_search, phenotype_statement, phenotype_experiment, term a, marker, term b, experiment, experiment_condition, fish
  where fmrel_ftr_zdb_id = genofeat_feature_zdb_id
  and fish_genotype_Zdb_id = genofeat_geno_zdb_id
  and mfs_genox_zdb_id = genox_zdb_id	
  and fish_zdb_id = genox_fish_zdb_id
  and mfs_mrkr_Zdb_id = fmrel_mrkr_Zdb_id
  and expcond_exp_zdb_id =exp_zdb_id
  and exp_zdb_id = genox_exp_zdb_id
  and fmrel_type = 'is allele of'
  and alltermcon_contained_zdb_id = phenos_entity_1_subterm_zdb_id
  and a.term_zdb_id = alltermcon_container_zdb_id
  and a.term_name in ('fin bud',
 'pelvic fin field' ,
 'pectoral fin field', 
 'paired fin skeleton', 
 'apical ectodermal ridge pectoral fin bud' ,
 'pectoral fin bud' ,
 'cleithrum' ,
 'mesocoracoid bone', 
 'pectoral fin cartilage', 
 'pelvic fin musculature' ,
 'superficial adductor' ,
 'coracoid' ,
 'pectoral girdle', 
 'postcleithrum' ,
 'superficial pelvic abductor', 
 'coracoradialis' ,
 'pelvic adductor profundus' ,
 'dorsal arrector' ,
 'pelvic radial' ,
 'posttemporal' ,
 'pectoral fin musculature' ,
 'pelvic abductor profundus' ,
 'pelvic girdle' ,
 'scapula' ,
 'superficial pelvic adductor' ,
 'supracleithrum' ,
 'abductor profundus', 
 'adductor profundus' ,
 'basipterygium' ,
 'dorsal pelvic arrector' ,
 'extrascapula' ,
 'superficial abductor' ,
 'ventral arrector' ,
 'ventral pelvic arrector' ,
 'mesoderm pectoral fin bud' ,
 'pectoral fin skeleton' ,
 'mesenchyme pectoral fin', 
 'pectoral fin' ,
 'pelvic fin' ,
 'pelvic fin bud',
 'apic1al ectodermal ridge pelvic fin bud' ,
 'mesoderm pelvic fin bud' ,
 'pelvic fin skeleton' ,
 'pelvic radial 2' ,
 'pelvic radial 3' ,
 'pelvic radial 1' ,
 'mesenchyme pelvic fin' ,
 'apical ectodermal ridge pelvic fin' ,
 'zone of polarizing activity pectoral fin bud' ,
 'zone of polarizing activity pelvic fin bud' ,
 'scapulocoracoid' ,
 'pectoral fin endoskeletal disc' ,
 'pelvic fin cartilage' ,
 'mesocoracoid cartilage' ,
 'pelvic radial cartilage' ,
 'basipterygium cartilage' ,
 'pelvic radial 3 cartilage', 
 'pelvic radial 2 cartilage' ,
 'pelvic radial 1 cartilage' ,
 'paired fin cartilage' ,
 'pectoral fin lepidotrichium', 
 'pelvic fin lepidotrichium' ,
 'pectoral fin radial',
 'pectoral fin proximal radial' ,
 'pectoral fin distal radial' ,
 'propterygium' ,
 'pectoral artery' ,
 'pectoral vein' ,
 'pectoral fin vasculature' ,
 'pectoral fin blood vessel',
 'pectoral fin lymph vessel',
 'fin fold pectoral fin bud',
 'pectoral fin fold',
 'pectoral fin nerve', 
 'pectoral fin motor nerve' ,
 'pectoral fin sensory nerve' ,
 'pectoral fin motor nerve 1' ,
 'pectoral fin motor nerve 2' ,
 'pectoral fin motor nerve 3' ,
 'pectoral fin motor nerve 4' ,
 'pectoral fin actinotrichium' ,
 'pelvic fin actinotrichium' ,
 'pectoral fin lepidotrichium 1' ,
 'pectoral fin lepidotrichium 2' ,
 'pectoral fin lepidotrichium 3' ,
 'pectoral fin lepidotrichium 6' ,
 'pectoral fin lepidotrichium 7' ,
 'pectoral fin lepidotrichium 5' ,
 'pectoral fin lepidotrichium 4' ,
 'pelvic fin lepidotrichium 1' ,
 'pelvic fin lepidotrichium 2' ,
 'pelvic fin lepidotrichium 4',
 'pelvic fin lepidotrichium 3',
 'paired fin',
 'primitive pectoral fin adductor',
 'pectoral fin development' ,
'pelvic fin development',
 'pectoral fin morphogenesis',
 'embryonic pectoral fin morphogenesis',
 'post-embryonic pectoral fin morphogenesis',
    'pelvic fin morphogenesis',
 'embryonic pelvic fin morphogenesis',
 'post-embryonic pelvic fin morphogenesis')
  and phenox_genox_zdb_id = genox_zdb_id
  and phenos_phenox_pk_id = phenox_pk_id
  and mrkr_zdb_id = mfs_mrkr_zdb_id
  and mrkr_zdb_id = fmrel_mrkr_zdb_id
 and b.term_zdb_id = alltermcon_contained_zdb_id
  and phenos_tag != 'normal'
;

insert into tmp_pheno_gene
select distinct phenos_pk_id, genox_Zdb_id,mrkr_abbrev, mrkr_zdb_id, b.term_ont_id, b.term_name, 'pheno', fish_genotype_zdb_id, expcond_mrkr_zdb_id, phenox_start_stg_zdb_id, phenox_end_stg_zdb_id
  from feature_marker_Relationship, genotype_Feature, all_term_contains, 
       fish_experiment, mutant_Fast_search, phenotype_statement, phenotype_experiment, term a, marker, term b,experiment, experiment_condition, fish
  where fmrel_ftr_zdb_id = genofeat_feature_zdb_id
  and fish_genotype_Zdb_id = genofeat_geno_zdb_id
  and expcond_exp_zdb_id =exp_zdb_id
  and exp_zdb_id = genox_exp_zdb_id
  and mfs_genox_zdb_id = genox_zdb_id	
  and fish_zdb_id = genox_fish_zdb_id
  and mfs_mrkr_Zdb_id = fmrel_mrkr_Zdb_id
  and fmrel_type = 'is allele of'
  and alltermcon_contained_zdb_id = phenos_entity_2_superterm_zdb_id
  and a.term_zdb_id = alltermcon_container_zdb_id
  and a.term_name in ('fin bud',
 'pelvic fin field' ,
 'pectoral fin field', 
 'paired fin skeleton', 
 'apical ectodermal ridge pectoral fin bud', 
 'pectoral fin bud' ,
 'cleithrum' ,
 'mesocoracoid bone', 
 'pectoral fin cartilage', 
 'pelvic fin musculature' ,
 'superficial adductor' ,
 'coracoid' ,
 'pectoral girdle', 
 'postcleithrum' ,
 'superficial pelvic abductor', 
 'coracoradialis' ,
 'pelvic adductor profundus' ,
 'dorsal arrector' ,
 'pelvic radial' ,
 'posttemporal' ,
 'pectoral fin musculature' ,
 'pelvic abductor profundus' ,
 'pelvic girdle' ,
 'scapula' ,
 'superficial pelvic adductor' ,
 'supracleithrum' ,
 'abductor profundus', 
 'adductor profundus' ,
 'basipterygium' ,
 'dorsal pelvic arrector' ,
 'extrascapula' ,
 'superficial abductor' ,
 'ventral arrector' ,
 'ventral pelvic arrector' ,
 'mesoderm pectoral fin bud' ,
 'pectoral fin skeleton' ,
 'mesenchyme pectoral fin', 
 'pectoral fin' ,
 'pelvic fin' ,
 'pelvic fin bud',
 'apic1al ectodermal ridge pelvic fin bud' ,
 'mesoderm pelvic fin bud' ,
 'pelvic fin skeleton' ,
 'pelvic radial 2' ,
 'pelvic radial 3' ,
 'pelvic radial 1' ,
 'mesenchyme pelvic fin' ,
 'apical ectodermal ridge pelvic fin' ,
 'zone of polarizing activity pectoral fin bud' ,
 'zone of polarizing activity pelvic fin bud' ,
 'scapulocoracoid' ,
 'pectoral fin endoskeletal disc' ,
 'pelvic fin cartilage' ,
 'mesocoracoid cartilage' ,
 'pelvic radial cartilage' ,
 'basipterygium cartilage' ,
 'pelvic radial 3 cartilage', 
 'pelvic radial 2 cartilage' ,
 'pelvic radial 1 cartilage' ,
 'paired fin cartilage' ,
 'pectoral fin lepidotrichium', 
 'pelvic fin lepidotrichium' ,
 'pectoral fin radial',
 'pectoral fin proximal radial' ,
 'pectoral fin distal radial' ,
 'propterygium' ,
 'pectoral artery' ,
 'pectoral vein' ,
 'pectoral fin vasculature' ,
 'pectoral fin blood vessel',
 'pectoral fin lymph vessel',
 'fin fold pectoral fin bud',
 'pectoral fin fold',
 'pectoral fin nerve', 
 'pectoral fin motor nerve' ,
 'pectoral fin sensory nerve' ,
 'pectoral fin motor nerve 1' ,
 'pectoral fin motor nerve 2' ,
 'pectoral fin motor nerve 3' ,
 'pectoral fin motor nerve 4' ,
 'pectoral fin actinotrichium' ,
 'pelvic fin actinotrichium' ,
 'pectoral fin lepidotrichium 1' ,
 'pectoral fin lepidotrichium 2' ,
 'pectoral fin lepidotrichium 3' ,
 'pectoral fin lepidotrichium 6' ,
 'pectoral fin lepidotrichium 7' ,
 'pectoral fin lepidotrichium 5' ,
 'pectoral fin lepidotrichium 4' ,
 'pelvic fin lepidotrichium 1' ,
 'pelvic fin lepidotrichium 2' ,
 'pelvic fin lepidotrichium 4',
 'pelvic fin lepidotrichium 3',
 'paired fin',
 'primitive pectoral fin adductor',
 'pectoral fin development' ,
'pelvic fin development',
 'pectoral fin morphogenesis',
 'embryonic pectoral fin morphogenesis',
 'post-embryonic pectoral fin morphogenesis',
    'pelvic fin morphogenesis',
 'embryonic pelvic fin morphogenesis',
 'post-embryonic pelvic fin morphogenesis')
  and phenox_genox_zdb_id = genox_zdb_id
  and phenos_phenox_pk_id = phenox_pk_id
  and mrkr_zdb_id = mfs_mrkr_zdb_id
  and mrkr_zdb_id = fmrel_mrkr_zdb_id
 and b.term_zdb_id = alltermcon_contained_zdb_id
  and phenos_tag != 'normal'
;

insert into tmp_pheno_gene
select distinct phenos_pk_id, genox_Zdb_id,mrkr_abbrev, mrkr_zdb_id, b.term_ont_id, b.term_name, 'pheno', fish_genotype_zdb_id, expcond_mrkr_zdb_id, phenox_start_stg_zdb_id, phenox_end_stg_zdb_id
  from feature_marker_Relationship, genotype_Feature, all_term_contains, 
       fish_experiment, mutant_Fast_search, phenotype_statement, phenotype_experiment, term a, marker, term b,experiment, experiment_condition, fish
  where fmrel_ftr_zdb_id = genofeat_feature_zdb_id
  and fish_genotype_Zdb_id = genofeat_geno_zdb_id
  and mfs_genox_zdb_id = genox_zdb_id	
  and fish_zdb_id = genox_fish_zdb_id
  and mfs_mrkr_Zdb_id = fmrel_mrkr_Zdb_id
  and fmrel_type = 'is allele of'
  and expcond_exp_zdb_id =exp_zdb_id
  and exp_zdb_id = genox_exp_zdb_id
  and alltermcon_contained_zdb_id = phenos_entity_2_subterm_zdb_id
  and a.term_zdb_id = alltermcon_container_zdb_id
  and a.term_name in ('fin bud',
 'pelvic fin field' ,
 'pectoral fin field', 
 'paired fin skeleton', 
 'apical ectodermal ridge pectoral fin bud', 
 'pectoral fin bud' ,
 'cleithrum' ,
 'mesocoracoid bone', 
 'pectoral fin cartilage', 
 'pelvic fin musculature' ,
 'superficial adductor' ,
 'coracoid' ,
 'pectoral girdle', 
 'postcleithrum' ,
 'superficial pelvic abductor', 
 'coracoradialis' ,
 'pelvic adductor profundus' ,
 'dorsal arrector' ,
 'pelvic radial' ,
 'posttemporal' ,
 'pectoral fin musculature' ,
 'pelvic abductor profundus' ,
 'pelvic girdle' ,
 'scapula' ,
 'superficial pelvic adductor' ,
 'supracleithrum' ,
 'abductor profundus', 
 'adductor profundus' ,
 'basipterygium' ,
 'dorsal pelvic arrector' ,
 'extrascapula' ,
 'superficial abductor' ,
 'ventral arrector' ,
 'ventral pelvic arrector' ,
 'mesoderm pectoral fin bud' ,
 'pectoral fin skeleton' ,
 'mesenchyme pectoral fin', 
 'pectoral fin' ,
 'pelvic fin' ,
 'pelvic fin bud',
 'apic1al ectodermal ridge pelvic fin bud' ,
 'mesoderm pelvic fin bud' ,
 'pelvic fin skeleton' ,
 'pelvic radial 2' ,
 'pelvic radial 3' ,
 'pelvic radial 1' ,
 'mesenchyme pelvic fin' ,
 'apical ectodermal ridge pelvic fin' ,
 'zone of polarizing activity pectoral fin bud' ,
 'zone of polarizing activity pelvic fin bud' ,
 'scapulocoracoid' ,
 'pectoral fin endoskeletal disc' ,
 'pelvic fin cartilage' ,
 'mesocoracoid cartilage' ,
 'pelvic radial cartilage' ,
 'basipterygium cartilage' ,
 'pelvic radial 3 cartilage', 
 'pelvic radial 2 cartilage' ,
 'pelvic radial 1 cartilage' ,
 'paired fin cartilage' ,
 'pectoral fin lepidotrichium', 
 'pelvic fin lepidotrichium' ,
 'pectoral fin radial',
 'pectoral fin proximal radial' ,
 'pectoral fin distal radial' ,
 'propterygium' ,
 'pectoral artery' ,
 'pectoral vein' ,
 'pectoral fin vasculature' ,
 'pectoral fin blood vessel',
 'pectoral fin lymph vessel',
 'fin fold pectoral fin bud',
 'pectoral fin fold',
 'pectoral fin nerve', 
 'pectoral fin motor nerve' ,
 'pectoral fin sensory nerve' ,
 'pectoral fin motor nerve 1' ,
 'pectoral fin motor nerve 2' ,
 'pectoral fin motor nerve 3' ,
 'pectoral fin motor nerve 4' ,
 'pectoral fin actinotrichium' ,
 'pelvic fin actinotrichium' ,
 'pectoral fin lepidotrichium 1' ,
 'pectoral fin lepidotrichium 2' ,
 'pectoral fin lepidotrichium 3' ,
 'pectoral fin lepidotrichium 6' ,
 'pectoral fin lepidotrichium 7' ,
 'pectoral fin lepidotrichium 5' ,
 'pectoral fin lepidotrichium 4' ,
 'pelvic fin lepidotrichium 1' ,
 'pelvic fin lepidotrichium 2' ,
 'pelvic fin lepidotrichium 4',
 'pelvic fin lepidotrichium 3',
 'paired fin',
 'primitive pectoral fin adductor',
 'pectoral fin development' ,
'pelvic fin development',
 'pectoral fin morphogenesis',
 'embryonic pectoral fin morphogenesis',
 'post-embryonic pectoral fin morphogenesis',
    'pelvic fin morphogenesis',
 'embryonic pelvic fin morphogenesis',
 'post-embryonic pelvic fin morphogenesis')
  and phenox_genox_zdb_id = genox_zdb_id
  and phenos_phenox_pk_id = phenox_pk_id
  and mrkr_zdb_id = mfs_mrkr_zdb_id
  and mrkr_zdb_id = fmrel_mrkr_zdb_id
  and b.term_zdb_id = alltermcon_contained_zdb_id
  and phenos_tag != 'normal'
;

insert into tmp_pheno_gene
select distinct xpatres_zdb_id, genox_Zdb_id,mrkr_abbrev, mrkr_zdb_id, b.term_ont_id,  b.term_name, 'xpat', fish_genotype_zdb_id, expcond_mrkr_zdb_id, xpatres_start_stg_zdb_id, xpatres_end_stg_zdb_id
  from expression_experiment, expression_Result, experiment,
       fish_experiment, genotype, all_term_contains,term a, marker, term b, experiment_condition, fish
  where xpatex_genox_Zdb_id = genox_zdb_id
  and fish_genotype_Zdb_id =geno_Zdb_id
  and fish_zdb_id = genox_fish_zdb_id
  and xpatres_xpatex_zdb_id = xpatex_Zdb_id
  and xpatres_expression_found = 't'
 and expcond_exp_zdb_id =exp_zdb_id
  and xpatres_superterm_zdb_id = alltermcon_contained_zdb_id
  and a.term_zdb_id = alltermcon_container_zdb_id 
  and geno_is_wildtype = 't'
  and exp_Zdb_id = genox_exp_zdb_id
  and exp_name in ('_Standard','_Generic-control')
  and mrkr_Zdb_id = xpatex_gene_zdb_id
and a.term_name in ('fin bud',
 'pelvic fin field' ,
 'pectoral fin field', 
 'paired fin skeleton', 
 'apical ectodermal ridge pectoral fin bud' ,
 'pectoral fin bud' ,
 'cleithrum' ,
 'mesocoracoid bone', 
 'pectoral fin cartilage', 
 'pelvic fin musculature' ,
 'superficial adductor' ,
 'coracoid' ,
 'pectoral girdle', 
 'postcleithrum' ,
 'superficial pelvic abductor', 
 'coracoradialis' ,
 'pelvic adductor profundus' ,
 'dorsal arrector' ,
 'pelvic radial' ,
 'posttemporal' ,
 'pectoral fin musculature' ,
 'pelvic abductor profundus' ,
 'pelvic girdle' ,
 'scapula' ,
 'superficial pelvic adductor' ,
 'supracleithrum' ,
 'abductor profundus', 
 'adductor profundus' ,
 'basipterygium' ,
 'dorsal pelvic arrector' ,
 'extrascapula' ,
 'superficial abductor' ,
 'ventral arrector' ,
 'ventral pelvic arrector' ,
 'mesoderm pectoral fin bud' ,
 'pectoral fin skeleton' ,
 'mesenchyme pectoral fin', 
 'pectoral fin' ,
 'pelvic fin' ,
 'pelvic fin bud',
 'apic1al ectodermal ridge pelvic fin bud' ,
 'mesoderm pelvic fin bud' ,
 'pelvic fin skeleton' ,
 'pelvic radial 2' ,
 'pelvic radial 3' ,
 'pelvic radial 1' ,
 'mesenchyme pelvic fin' ,
 'apical ectodermal ridge pelvic fin' ,
 'zone of polarizing activity pectoral fin bud' ,
 'zone of polarizing activity pelvic fin bud' ,
 'scapulocoracoid' ,
 'pectoral fin endoskeletal disc' ,
 'pelvic fin cartilage' ,
 'mesocoracoid cartilage' ,
 'pelvic radial cartilage' ,
 'basipterygium cartilage' ,
 'pelvic radial 3 cartilage', 
 'pelvic radial 2 cartilage' ,
 'pelvic radial 1 cartilage' ,
 'paired fin cartilage' ,
 'pectoral fin lepidotrichium', 
 'pelvic fin lepidotrichium' ,
 'pectoral fin radial',
 'pectoral fin proximal radial' ,
 'pectoral fin distal radial' ,
 'propterygium' ,
 'pectoral artery' ,
 'pectoral vein' ,
 'pectoral fin vasculature' ,
 'pectoral fin blood vessel',
 'pectoral fin lymph vessel',
 'fin fold pectoral fin bud',
 'pectoral fin fold',
 'pectoral fin nerve', 
 'pectoral fin motor nerve' ,
 'pectoral fin sensory nerve' ,
 'pectoral fin motor nerve 1' ,
 'pectoral fin motor nerve 2' ,
 'pectoral fin motor nerve 3' ,
 'pectoral fin motor nerve 4' ,
 'pectoral fin actinotrichium' ,
 'pelvic fin actinotrichium' ,
 'pectoral fin lepidotrichium 1' ,
 'pectoral fin lepidotrichium 2' ,
 'pectoral fin lepidotrichium 3' ,
 'pectoral fin lepidotrichium 6' ,
 'pectoral fin lepidotrichium 7' ,
 'pectoral fin lepidotrichium 5' ,
 'pectoral fin lepidotrichium 4' ,
 'pelvic fin lepidotrichium 1' ,
 'pelvic fin lepidotrichium 2' ,
 'pelvic fin lepidotrichium 4',
 'pelvic fin lepidotrichium 3',
 'paired fin',
 'primitive pectoral fin adductor',
 'pectoral fin development' ,
'pelvic fin development',
 'pectoral fin morphogenesis',
 'embryonic pectoral fin morphogenesis',
 'post-embryonic pectoral fin morphogenesis',
    'pelvic fin morphogenesis',
 'embryonic pelvic fin morphogenesis',
 'post-embryonic pelvic fin morphogenesis')
 and b.term_zdb_id = alltermcon_contained_zdb_id;

insert into tmp_pheno_gene
select distinct xpatres_zdb_id, genox_Zdb_id,mrkr_abbrev, mrkr_zdb_id, b.term_ont_id, b.term_name, 'xpat', fish_genotype_zdb_id, expcond_mrkr_zdb_id, xpatres_start_stg_zdb_id, xpatres_end_stg_zdb_id
  from expression_experiment, expression_Result, experiment, experiment_condition,
       fish_experiment, genotype, all_term_contains,term a,term b, marker, fish
  where xpatex_genox_Zdb_id = genox_zdb_id
  and fish_genotype_Zdb_id =geno_Zdb_id
  and fish_zdb_id = genox_fish_zdb_id
  and xpatres_xpatex_zdb_id = xpatex_Zdb_id
  and expcond_exp_zdb_id = exp_Zdb_id
  and xpatres_expression_found = 't'
  and xpatres_subterm_zdb_id = alltermcon_contained_zdb_id
  and a.term_zdb_id = alltermcon_container_zdb_id 
  and geno_is_wildtype = 't'
  and exp_Zdb_id = genox_exp_zdb_id
  and exp_name in ('_Standard','_Generic-control')
  and mrkr_zdb_id = xpatex_gene_zdb_id
and a.term_name in ('fin bud',
 'pelvic fin field' ,
 'pectoral fin field', 
 'paired fin skeleton', 
 'apical ectodermal ridge pectoral fin bud', 
 'pectoral fin bud' ,
 'cleithrum' ,
 'mesocoracoid bone', 
 'pectoral fin cartilage', 
 'pelvic fin musculature' ,
 'superficial adductor' ,
 'coracoid' ,
 'pectoral girdle', 
 'postcleithrum' ,
 'superficial pelvic abductor', 
 'coracoradialis' ,
 'pelvic adductor profundus' ,
 'dorsal arrector' ,
 'pelvic radial' ,
 'posttemporal' ,
 'pectoral fin musculature' ,
 'pelvic abductor profundus' ,
 'pelvic girdle' ,
 'scapula' ,
 'superficial pelvic adductor' ,
 'supracleithrum' ,
 'abductor profundus', 
 'adductor profundus' ,
 'basipterygium' ,
 'dorsal pelvic arrector' ,
 'extrascapula' ,
 'superficial abductor' ,
 'ventral arrector' ,
 'ventral pelvic arrector' ,
 'mesoderm pectoral fin bud' ,
 'pectoral fin skeleton' ,
 'mesenchyme pectoral fin', 
 'pectoral fin' ,
 'pelvic fin' ,
 'pelvic fin bud',
 'apic1al ectodermal ridge pelvic fin bud' ,
 'mesoderm pelvic fin bud' ,
 'pelvic fin skeleton' ,
 'pelvic radial 2' ,
 'pelvic radial 3' ,
 'pelvic radial 1' ,
 'mesenchyme pelvic fin' ,
 'apical ectodermal ridge pelvic fin' ,
 'zone of polarizing activity pectoral fin bud' ,
 'zone of polarizing activity pelvic fin bud' ,
 'scapulocoracoid' ,
 'pectoral fin endoskeletal disc' ,
 'pelvic fin cartilage' ,
 'mesocoracoid cartilage' ,
 'pelvic radial cartilage' ,
 'basipterygium cartilage' ,
 'pelvic radial 3 cartilage', 
 'pelvic radial 2 cartilage' ,
 'pelvic radial 1 cartilage' ,
 'paired fin cartilage' ,
 'pectoral fin lepidotrichium', 
 'pelvic fin lepidotrichium' ,
 'pectoral fin radial',
 'pectoral fin proximal radial' ,
 'pectoral fin distal radial' ,
 'propterygium' ,
 'pectoral artery' ,
 'pectoral vein' ,
 'pectoral fin vasculature' ,
 'pectoral fin blood vessel',
 'pectoral fin lymph vessel',
 'fin fold pectoral fin bud',
 'pectoral fin fold',
 'pectoral fin nerve', 
 'pectoral fin motor nerve' ,
 'pectoral fin sensory nerve' ,
 'pectoral fin motor nerve 1' ,
 'pectoral fin motor nerve 2' ,
 'pectoral fin motor nerve 3' ,
 'pectoral fin motor nerve 4' ,
 'pectoral fin actinotrichium' ,
 'pelvic fin actinotrichium' ,
 'pectoral fin lepidotrichium 1' ,
 'pectoral fin lepidotrichium 2' ,
 'pectoral fin lepidotrichium 3' ,
 'pectoral fin lepidotrichium 6' ,
 'pectoral fin lepidotrichium 7' ,
 'pectoral fin lepidotrichium 5' ,
 'pectoral fin lepidotrichium 4' ,
 'pelvic fin lepidotrichium 1' ,
 'pelvic fin lepidotrichium 2' ,
 'pelvic fin lepidotrichium 4',
 'pelvic fin lepidotrichium 3',
 'paired fin',
 'primitive pectoral fin adductor',
 'pectoral fin development' ,
'pelvic fin development',
 'pectoral fin morphogenesis',
 'embryonic pectoral fin morphogenesis',
 'post-embryonic pectoral fin morphogenesis',
    'pelvic fin morphogenesis',
 'embryonic pelvic fin morphogenesis',
 'post-embryonic pelvic fin morphogenesis')
 and b.term_zdb_id = alltermcon_contained_zdb_id
;


select first 1 * from tmp_pheno_gene
  where id like 'ZDB%';


create temp table tmp_xpat (gene_abbrev varchar(50), start_stg varchar(50), end_stg varchar(50), term_name varchar(100), whereFrom varchar(50))
with no log;


--id varchar(50), gene_abbrev varchar(50), gene_zdb_id varchar(50), term_ont_id varchar(30), term_name varchar(100), whereFrom varchar(20), geno_id varchar(50), mo_id varchar(50), stage_start_id varchar(50), stage_end_id varchar(50)

UNLOAD to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpatGenesPipe.txt'
DELIMITER "	"
  select gene_abbrev, gene_zdb_id, mo_id, a.term_ont_id, a.term_name, b.term_ont_id, b.term_name, geno_id, geno_display_name, stage_start_id, stage_end_id, xpatex_source_zdb_id
    from tmp_pheno_gene,  expression_result, expression_experiment, term a, term b, genotype
    where xpatex_zdb_id = xpatres_xpatex_zdb_id
   and id = xpatres_zdb_id
   and geno_Zdb_id = geno_id
   and xpatres_superterm_zdb_id = a.term_zdb_id
   and xpatres_subterm_zdb_id = b.term_zdb_id
   and xpatres_subterm_zdb_id is not null
   and whereFrom like 'xpat%'
union
  select gene_abbrev, gene_zdb_id, mo_id, a.term_ont_id, a.term_name,"","", geno_id, geno_display_name, stage_start_id, stage_end_id, xpatex_source_zdb_id
    from tmp_pheno_gene,  expression_result, expression_experiment, term a, genotype
    where xpatex_zdb_id = xpatres_xpatex_zdb_id
   and id = xpatres_zdb_id
   and geno_id = geno_zdb_id
   and xpatres_superterm_zdb_id = a.term_zdb_id
   and xpatres_subterm_zdb_id is null
   and whereFrom like 'xpat%'	

;


create temp table tmp_pheno (gene_abbrev varchar(50),  term_name varchar(100), patoTerm varchar(100), whereFrom varchar(50))
with no log;

insert into tmp_pheno
  select gene_abbrev,s.term_name,c.term_name, whereFrom
  from tmp_pheno_gene s, phenotype_statement, term c
  where c.term_zdb_id = phenos_quality_zdb_id
  and id = phenos_pk_id
  and whereFrom like 'pheno%';

create temp table tmp_dumpPheno (
       pheno_id serial,
       gene_abbrev varchar(50),
       gene_zdb_id varchar(50), 
       asuperterm_ont_id varchar(30), 
       asuperterm_name varchar(255),
       asubterm_ont_id varchar(30),
       asubterm_name varchar(255),
       bsuperterm_ont_id varchar(30), 
       bsuperterm_name varchar(255),
       bsubterm_ont_id varchar(30), 
       bsubterm_name varchar(255),
       quality_id varchar(30),
       quality_name varchar(255),
       whereFrom varchar(20), 
       geno_id varchar(50),
       geno_display_name varchar(255), 
       mo_id varchar(50), 
       stage_start_id varchar(50), 
       stage_end_id varchar(50),
       genox_id varchar(50),
       pub_id varchar(50),
       fig_id varchar(50)
)
with no log;

! echo "add superterm 1 "
insert into tmp_dumpPheno (pheno_id,genox_id, gene_abbrev, gene_zdb_id, geno_id, mo_id, stage_start_id, stage_end_id, asuperterm_ont_id, asuperterm_name, pub_id, fig_id)
  select distinct id, genox_zdb_id, gene_abbrev, gene_zdb_id, geno_id, mo_id, stage_start_id, stage_end_id, term.term_ont_id, term.term_name, fig_source_zdb_id, fig_zdb_id
    from tmp_pheno_gene, phenotype_statement, phenotype_experiment, term, figure
    where id = phenos_pk_id
    and fig_zdb_id = phenox_fig_zdb_id
    and phenox_pk_id = phenos_phenox_pk_id
    and phenos_entity_1_superterm_zdb_id = term_zdb_id
 and whereFrom like 'pheno%';

! echo "add subterm 1 "
insert into tmp_dumpPheno (pheno_id,genox_id, gene_abbrev, gene_zdb_id, geno_id, mo_id, stage_start_id, stage_end_id, asuperterm_ont_id, asuperterm_name, pub_id, fig_id)
  select distinct id, genox_zdb_id, gene_abbrev, gene_zdb_id, geno_id, mo_id, stage_start_id, stage_end_id, term.term_ont_id, term.term_name, fig_source_zdb_id, fig_zdb_id
    from tmp_pheno_gene, phenotype_statement, phenotype_experiment, term, figure
    where id = phenos_pk_id
    and fig_zdb_id = phenox_fig_zdb_id
    and phenox_pk_id = phenos_phenox_pk_id
    and phenos_entity_1_subterm_zdb_id = term_zdb_id
 and whereFrom like 'pheno%';

! echo "add superterm 2 "
insert into tmp_dumpPheno (pheno_id,genox_id, gene_abbrev, gene_zdb_id, geno_id, mo_id, stage_start_id, stage_end_id, asuperterm_ont_id, asuperterm_name, pub_id, fig_id)
  select distinct id, genox_zdb_id, gene_abbrev, gene_zdb_id, geno_id, mo_id, stage_start_id, stage_end_id, term.term_ont_id, term.term_name, fig_source_zdb_id, fig_zdb_id
    from tmp_pheno_gene, phenotype_statement, phenotype_experiment, term, figure
    where id = phenos_pk_id
    and fig_zdb_id = phenox_fig_zdb_id
    and phenox_pk_id = phenos_phenox_pk_id
    and phenos_entity_2_superterm_zdb_id = term_zdb_id
 and whereFrom like 'pheno%';

! echo "add subterm 2 "
insert into tmp_dumpPheno (pheno_id,genox_id, gene_abbrev, gene_zdb_id, geno_id, mo_id, stage_start_id, stage_end_id, asuperterm_ont_id, asuperterm_name, pub_id, fig_id)
  select distinct id, genox_zdb_id, gene_abbrev, gene_zdb_id, geno_id, mo_id, stage_start_id, stage_end_id, term.term_ont_id, term.term_name, fig_source_zdb_id, fig_zdb_id
    from tmp_pheno_gene, phenotype_statement, phenotype_experiment, term, figure
    where id = phenos_pk_id
    and fig_zdb_id = phenox_fig_zdb_id
    and phenox_pk_id = phenos_phenox_pk_id
    and phenos_entity_2_subterm_zdb_id = term_zdb_id
 and whereFrom like 'pheno%';

update tmp_dumpPheno
  set geno_display_name = (select geno_display_name
      			  	  from genotype
				  where geno_zdb_id = geno_id);


UNLOAD to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/phenoGene.txt'
 DELIMITER "	"
  select
   pheno_id,gene_abbrev, gene_zdb_id,
   tps.asubterm_ont_id,tps.asubterm_name,
	 tps.arelationship_id,
	 tps.arelationship_name,
	 tps.asuperterm_ont_id,tps.asuperterm_name,
	 tps.quality_id,tps.quality_name,quality_tag,
	 tps.bsubterm_ont_id,tps.bsubterm_name,
	 tps.brelationship_id,
	 tps.brelationship_name,
	 tps.bsuperterm_ont_id,tps.bsuperterm_name,
   geno_id,
   geno_display_name,
   mo_id,
   stage_start_id,
   stage_end_id,
   genox_id,
   pub_id,
   fig_id
   from tmp_dumpPheno pheno, tmp_phenotype_statement tps
  where pheno_id = phenos_pk_id
  	 order by gene_abbrev
;


--commit work;

rollback work ;
