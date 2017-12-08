begin work ;


-------------------------------------------------------------- Genes --------------------------------------------------------------------------
-- Genes
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Genes', 'Genes', now() from marker
 where substring(mrkr_type from 1 for 4) = 'GENE' or mrkr_type like '%RNAG%'
--and mrkr_zdb_id not like 'ZDB-%-12____-%'
;
-- Genes on Vega Assembly
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct mrel_mrkr_1_zdb_id), 'Genes', 'Genes on Assembly', now()
 from marker_relationship
 where mrel_type = 'gene produces transcript'
;

-- Transcripts
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Genes', 'Transcripts', now() from db_link
 where substring(dblink_acc_num from 1 for 8) = 'OTTDART0'
;
--
--grep OTTDART0 db_link | wc -l

-- EST/cDNAs
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Genes', 'EST/cDNAs', now() from marker
 where mrkr_type in ('EST','CDNA')
--and mrkr_zdb_id not like 'ZDB-%-12____-%'
;

--------------------------------------------------------------------- Genetics----------------------
-- Features          (Alleles)
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Genetics', 'Features', now() from feature --alteration -- fish
--where feature_zdb_id not like 'ZDB-%-12____-%'
;

--  Transgenic Features
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Genetics', 'Transgenic Features', now()
from feature
where feature_type = 'TRANSGENIC_INSERTION'
  and feature_name not like '%;%'
;

--  Transgenic Construct
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct mrkr_zdb_id), 'Genetics', 'Transgenic Constructs', now()
from marker
where mrkr_type in  ('TGCONSTRCT','PTCONSTRCT','GTCONSTRCT','ETCONSTRCT')
  and mrkr_abbrev not like '%;%'
;


--  Transgenic Genotypes
insert into annual_stats(as_count, as_section, as_type, as_date)
select  count(distinct genofeat_geno_zdb_id), 'Genetics', 'Transgenic Genotypes', now()
 from genotype_feature, feature
 where genofeat_feature_zdb_id = feature_zdb_id
   and feature_type = 'TRANSGENIC_INSERTION'
;

-- Genotypes (non-Wildtype)
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Genetics', 'Genotypes', now()
 from genotype where  geno_is_wildtype = 'f'
;


-- Genes with GO annotation
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct mrkrgoev_mrkr_zdb_id), 'Genetics', 'Genes with GO annotations', now() from marker_go_term_evidence
-- cut -f 2 -d\| < marker_go_term_evidence | sort -u | wc -l
;

-- IEA GO annotations
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct mrkrgoev_mrkr_zdb_id), 'Genetics', 'Genes with IEA GO annotations', now() from marker_go_term_evidence
where mrkrgoev_evidence_code = 'IEA'
-- cut -f 2,5 -d\| < marker_go_term_evidence | grep '|IEA' |sort -u | wc -l

;

-- Non-IEA GO annotations
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct mrkrgoev_mrkr_zdb_id), 'Genetics', 'Genes with Non-IEA GO anotations', now() 
from marker_go_term_evidence
where mrkrgoev_evidence_code != 'IEA'
-- cut -f 2,5 -d\| < marker_go_term_evidence | grep -v 'IEA' | cut -f1 -d \|| sort -u | wc -l

;

-- Total GO annotations
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Genetics', 'Total GO Annotations', now() from marker_go_term_evidence
-- wc -l marker_go_term_evidence

;

--Genes with OMIM disease phenotypes
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct omimp_ortho_zdb_id), 'Genetics', 'Genes with OMIM phenotypes', now()
 from omim_phenotype ;


---------------------------------------------------------------Reagents ----------------------------
--Morpholinos
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Reagents', 'Morpholinos', now() from marker
 where substring(mrkr_zdb_id from 1 for 12) = 'ZDB-MRPHLNO-'
--and mrkr_zdb_id not like 'ZDB-%-12____-%'
; --grep 'ZDB-MRPHLNO-' marker | wc -l

--TALEN
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Reagents', 'TALEN', now() from marker
 where mrkr_zdb_id like 'ZDB-TALEN%';

--CRISPR
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Reagents', 'CRISPR', now() from marker
  where mrkr_zdb_id like 'ZDB-CRISPR%';

-- Antibodies
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Reagents', 'Antibodies', now() from marker
 where substring(mrkr_zdb_id from 1 for 8) = 'ZDB-ATB-'
--and mrkr_zdb_id not like 'ZDB-%-12____-%'
; --grep 'ZDB-ATB-' marker | wc -l



-- Wild-type strains (24) skip
--select count(*)wildtype from fish where line_type != 'mutant'
--select count(*) wildtype
-- from genotype where geno_is_wildtype
--and geno_zdb_id not like 'ZDB-%-10____-%'
--;

------------------------------------------Expression & Phenotypes---------------------------------
-- Gene expression patterns
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Expression & Phenotype', 'Gene expression patterns', now() from expression_experiment2
--
where xpatex_zdb_id not like 'ZDB-%-10____-%'
;

-- clean Gene expression patterns
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct xpatex_gene_zdb_id), 'Expression & Phenotype', 'Genes with expression data', now() from expression_experiment2
  from expression_experiment2 where exists (select 'x' from clean_expression_fast_search where cefs_genox_zdb_id = xpatex_genox_zdb_id);


--Phenotype Statements
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Expression & Phenotype', 'Phenotype statements', now() from phenotype_statement;


--Clean phenotype
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct (mfs_mrkr_Zdb_id)), 'Expression & Phenotype', 'Genes with a phenotype', now() from phenotype_statement, phenotype_Experiment, mutant_fast_search
 where phenox_pk_id = phenos_phenox_pk_id 
 and phenox_genox_zdb_id = mfs_genox_zdb_id
and (mfs_mrkr_zdb_id like 'ZDB-GENE%' or mfs_mrkr_zdb_id like '%RNAG%');

-- Images annotated for expression
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Expression & Phenotype', 'Images', now() from image
 --fish_image
--where img_zdb_id not like 'ZDB-%-12____-%'
;


-- Anatomical structures
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Expression & Phenotype', 'Anatomical structures', now() from term
 where term_ontology in ('zebrafish_anatomy','zebrafish_anatomical_ontology');


-- Mapped markers
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Genomics', 'Mapped markers', now() from paneled_markers;

insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Genomics', 'Links to other databases', now() from db_link, foreign_db_contains,foreign_db
 where fdbcont_fdb_db_id = fdb_db_pk_id
   and  dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and  fdb_db_name in (
                       --2011-12-16
------------------------- ---------
'Curated miRNA Mature', --                  41
'Curated miRNA Stem Loop', --                3
'EBI-Cell', --                             776
'EC', --                                   858
'Ensembl(GRCz10)', --                       16279
'Ensembl_SNP', --                         2024
'Ensembl_Trans', --                      27310
'FLYBASE', --                               55
'GenBank', --                           160821
'GenPept', --                            43839
'Gene', --                               42124
'InterPro', --                           49367
'MGI', --                                 8343
'MODB', --                                 100
'MicroCosm', --                            219
'NCBO-CARO', --                             58
'OMIM', --                                8498
'PROSITE', --                            18481
'PUBPROT', --                              113
'PUBRNA', --                                 2
'Pfam', --                               19694
'RefSeq', --                             31466
'SGD', --                                    9
'Sanger_Clone', --                        4247
'UniGene', --                            22272
'UniProtKB', --                          25865
'UniSTS', --                              1777
'VEGA', --                               28392
'VEGAPROT', --                           21492
'VEGA_Clone', --                         11468
'Vega_Trans', --                         28563
'Vega_Withdrawn', --                      1822
'WashUZ', --                               548
'ZFIN_PROT', --                             23
'dbSNP', --                               2024
'miRBASE Mature', --                       218
'miRBASE Stem Loop', --                    336
'unreleasedRNA' --                         12
)
--and dblink_zdb_id not like 'ZDB-%-12____-%'
;

-----------------------Community information-------------------------------------
-- Publications
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Community information', 'Publications', now() from publication
--where zdb_id not like 'ZDB-%-12____-%'
;

-- Researchers
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Community information', 'Researchers', now() from person
--where zdb_id not like 'ZDB-%-12____-%'
;

-- Laboratories
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Community information', 'Laboratories', now() from lab
--where zdb_id not like 'ZDB-%-12____-%'
;

-- Companies
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), 'Community information', 'Companies', now() from company
--where zdb_id not like 'ZDB-%-12____-%'
;

---------------------------------------------------------------- Orthology ---------------------

--Genes w/Human Orthology
insert into annual_stats(as_count, as_section, as_type, as_date)
select count( distinct ortho_zebrafish_gene_zdb_id), 'Orthology', 'Genes w/Human Orthology', now()
 from ortholog_evidence, organism, ortholog
  where organism_common_name like '%Human%'
 and organism_taxid = ortho_other_species_taxid
 and ortho_Zdb_id = oev_ortho_Zdb_id
;


--Genes w/Mouse Orthology
insert into annual_stats(as_count, as_section, as_type, as_date)
select count( distinct ortho_zebrafish_gene_zdb_id), 'Orthology', 'Genes w/Mouse Orthology', now()
 from ortholog_evidence, organism, ortholog
  where organism_common_name like '%Mouse%'
 and organism_taxid = ortho_other_species_taxid
 and ortho_Zdb_id = oev_ortho_Zdb_id
;

\copy (select year(as_date), as_pk_id, as_section, as_type, as_count from annual_stats order by year(as_date) desc, as_pk_id asc) to 'stats.txt' delimiter '|';

commit work ;
