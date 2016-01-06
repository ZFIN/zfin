begin work ;

create temp table tmp_output (counter int8, section varchar(100))
with no log;

{

-- shell commmands run against
cd /research/zunloads/databases/zfindb/2012.12.31.1/

### ZDB-GENE & evid
cut -f 2 -d\| < marker_go_term_evidence | sort -u | wc -l
18100
cut -f 2,4 -d\| < marker_go_term_evidence | grep "|IEA" |sort -u | wc -l
14658
cut -f 2,4 -d\| < marker_go_term_evidence | grep -v "IEA" | cut -f1 -d \|| sort -u | wc -l
9014
wc -l marker_go_term_evidence
135314
wc -l db_link
674508
grep OTTDART0 db_link | wc -l
32841

}

-------------------------------------------------------------- Genes --------------------------------------------------------------------------
-- Genes
insert into tmp_output(counter, section)
select count(*), "genes" from marker
 where mrkr_type[1,4] = 'GENE'
--and mrkr_zdb_id not like "ZDB-%-12____-%"
;
-- Genes on Vega Assembly
insert into tmp_output(counter, section)
select count(distinct mrel_mrkr_1_zdb_id), "gene_on_assembly"
 from marker_relationship
 where mrel_type == 'gene produces transcript'
;

-- Transcripts
insert into tmp_output(counter, section)
select count(*), "transcripts" from db_link
 where dblink_acc_num[1,8] = 'OTTDART0'
;
--
--grep OTTDART0 db_link | wc -l

-- EST/cDNAs
insert into tmp_output(counter, section)
select count(*), "EST/cDNA" from marker
 where mrkr_type in ('EST','CDNA')
--and mrkr_zdb_id not like 'ZDB-%-12____-%'
;

--------------------------------------------------------------------- Genetics----------------------
-- Features          (Alleles)
insert into tmp_output(counter, section)
select count(*), "alleles" from feature --alteration -- fish
--where feature_zdb_id not like 'ZDB-%-12____-%'
;

--  Transgenic Features
insert into tmp_output(counter, section)
select count(*), "Transgenic Features"
from feature
where feature_type = 'TRANSGENIC_INSERTION'
  and feature_name not like "%;%"
;

--  Transgenic Construct
insert into tmp_output(counter, section)
select count(distinct mrkr_zdb_id), "Transgenic Constructs"
from marker
where mrkr_type in  ('TGCONSTRCT','PTCONSTRCT','GTCONSTRCT','ETCONSTRCT')
  and mrkr_abbrev not like "%;%"
;


--  Transgenic Genotypes
insert into tmp_output(counter, section)
select  count(distinct genofeat_geno_zdb_id), "Transgenic Genotypes"
 from genotype_feature, feature
 where genofeat_feature_zdb_id = feature_zdb_id
   and feature_type = 'TRANSGENIC_INSERTION'
;

-- Genotypes (non-Wildtype)
insert into tmp_output(counter, section)
select count(*), "Non-Wildtype, Genotypes"
 from genotype where  geno_is_wildtype == 'f'
;


-- Genes with GO annotation
insert into tmp_output(counter, section)
select count(distinct mrkrgoev_mrkr_zdb_id), "Genes With GO Annotation" from marker_go_term_evidence
-- cut -f 2 -d\| < marker_go_term_evidence | sort -u | wc -l
;

-- IEA GO annotations
insert into tmp_output(counter, section)
select count(distinct mrkrgoev_mrkr_zdb_id), "IEA GO Annotations" from marker_go_term_evidence
where mrkrgoev_evidence_code = 'IEA'
-- cut -f 2,5 -d\| < marker_go_term_evidence | grep "|IEA" |sort -u | wc -l

;

-- Non-IEA GO annotations
insert into tmp_output(counter, section)
select count(distinct mrkrgoev_mrkr_zdb_id), "Non-IEA GO Annotations" 
from marker_go_term_evidence
where mrkrgoev_evidence_code != 'IEA'
-- cut -f 2,5 -d\| < marker_go_term_evidence | grep -v "IEA" | cut -f1 -d \|| sort -u | wc -l

;

-- Total GO annotations
insert into tmp_output(counter, section)
select count(*), "All GO Annotations" from marker_go_term_evidence
-- wc -l marker_go_term_evidence

;

--Genes with OMIM disease phenotypes
insert into tmp_output(counter, section)
select count(distinct omimp_ortho_zdb_id), "Human Orthology With OMIM Disease Phenotypes"
 from omim_phenotype ;


---------------------------------------------------------------Reagents ----------------------------
--Morpholinos
insert into tmp_output(counter, section)
select count(*), "morpholinos" from marker
 where mrkr_zdb_id[1,12] = 'ZDB-MRPHLNO-'
--and mrkr_zdb_id not like 'ZDB-%-12____-%'
; --grep 'ZDB-MRPHLNO-' marker | wc -l

--TALEN
insert into tmp_output(counter, section)
select count(*), "talens" from marker
 where mrkr_zdb_id like 'ZDB-TALEN%';

--CRISPR
insert into tmp_output(counter, section)
select count(*), "crisprs" from marker
  where mrkr_zdb_id like 'ZDB-CRISPR%';

-- Antibodies
insert into tmp_output(counter, section)
select count(*), "antibodies" from marker
 where mrkr_zdb_id[1,8] = 'ZDB-ATB-'
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
insert into tmp_output(counter, section)
select count(*), "expression patterns" from expression_experiment2
--
where xpatex_zdb_id not like 'ZDB-%-10____-%'
;

-- clean Gene expression patterns
insert into tmp_output(counter, section)
select count(distinct xpatex_gene_zdb_id), "genes clean expression patterns" from expression_experiment2, clean_expression_fast_search
  where xpatex_genox_zdb_id = cefs_genox_zdb_id;


--Phenotype Statements
insert into tmp_output(counter, section)
select count(*), "phenotype statements" from phenotype_statement;


--Clean phenotype
insert into tmp_output(counter, section)
select count(distinct (mfs_mrkr_Zdb_id)), "clean phenotype statements" from phenotype_statement, phenotype_Experiment, mutant_fast_search
 where phenox_pk_id = phenos_phenox_pk_id 
 and phenox_genox_zdb_id = mfs_genox_zdb_id
and mfs_mrkr_zdb_id like 'ZDB-GENE%';

-- Images annotated for expression
insert into tmp_output(counter, section)
select count(*), "fish images" from image
 --fish_image
--where img_zdb_id not like 'ZDB-%-12____-%'
;


-- Anatomical structures
insert into tmp_output(counter, section)
select count(*), "anatomical structures" from term
 where term_ontology in ('zebrafish_anatomy','zebrafish_anatomical_ontology');
--where anatitem_zdb_id not like 'ZDB-%-12____-%'


-- Developmental stages select count(*) stage from stage
--where stg_zdb_id not like 'ZDB-%-12____-%';
------------------------------Genomics------------------------------------------
-- Mapping panels select count(*) panel from panels
--where zdb_id not like 'ZDB-%-12____-%';

-- Mapped markers
insert into tmp_output(counter, section)
select count(*), "mapped markers" from paneled_markers
--where zdb_id not like 'ZDB-%-12____-%'
;

-- Links to other databases
-- look at the set of db names to count, filter as needed
{***********************************************************************}
--select  ('"'||fdb_db_name||'", --')::varchar(29), count(*)
-- from  db_link, foreign_db_contains,foreign_db
-- where fdbcont_fdb_db_id == fdb_db_pk_id
--  and  dblink_fdbcont_zdb_id = fdbcont_zdb_id
-- group by 1 order by 1;
--
--
--select fdb_db_name[1,24] name, fdbdt_data_type[1,17] type,
--          fdbcont_zdb_id[1,22] zdb_id, fdbcont_fdb_db_id::int  id,
--          fdbdt_super_type[1,12] supertype
-- from foreign_db_contains, foreign_db,foreign_db_data_type
-- where fdbcont_organism_common_name = 'Zebrafish'
--   and fdbcont_fdbdt_id == fdbdt_pk_id
--   and fdbdt_super_type in ('sequence','summary page')
--   and fdbcont_fdb_db_id == fdb_db_pk_id
--  order by 5,1,2,3
--;
{***********************************************************************}
insert into tmp_output(counter, section)
select count(*), "externalLinks" from db_link, foreign_db_contains,foreign_db
 where fdbcont_fdb_db_id == fdb_db_pk_id
   and  dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and  fdb_db_name in (
                       --2011-12-16
------------------------- ---------
"Curated miRNA Mature", --                  41
"Curated miRNA Stem Loop", --                3
"EBI-Cell", --                             776
"EC", --                                   858
"Ensembl(GRCz10)", --                       16279
"Ensembl_SNP", --                         2024
"Ensembl_Trans", --                      27310
"FLYBASE", --                               55
"GenBank", --                           160821
"GenPept", --                            43839
"Gene", --                               42124
"InterPro", --                           49367
"MGI", --                                 8343
"MODB", --                                 100
"MicroCosm", --                            219
"NCBO-CARO", --                             58
"OMIM", --                                8498
"PROSITE", --                            18481
"PUBPROT", --                              113
"PUBRNA", --                                 2
"Pfam", --                               19694
"RefSeq", --                             31466
"SGD", --                                    9
"Sanger_Clone", --                        4247
"UniGene", --                            22272
"UniProtKB", --                          25865
"UniSTS", --                              1777
"VEGA", --                               28392
"VEGAPROT", --                           21492
"VEGA_Clone", --                         11468
"Vega_Trans", --                         28563
"Vega_Withdrawn", --                      1822
"WashUZ", --                               548
"ZFIN_PROT", --                             23
"dbSNP", --                               2024
"miRBASE Mature", --                       218
"miRBASE Stem Loop", --                    336
"unreleasedRNA" --                         12
)
--and dblink_zdb_id not like 'ZDB-%-12____-%'
;

-----------------------Community information-------------------------------------
-- Publications
insert into tmp_output(counter, section)
select count(*), "publications" from publication
--where zdb_id not like 'ZDB-%-12____-%'
;

-- Researchers
insert into tmp_output(counter, section)
select count(*), "researchers" from person
--where zdb_id not like 'ZDB-%-12____-%'
;

-- Laboratories
insert into tmp_output(counter, section)
select count(*), "laboratories" from lab
--where zdb_id not like 'ZDB-%-12____-%'
;

-- Companies
insert into tmp_output(counter, section)
select count(*), "companies" from company
--where zdb_id not like 'ZDB-%-12____-%'
;

---------------------------------------------------------------- Orthology ---------------------

--Genes w/Human Orthology
insert into tmp_output(counter, section)
select count( distinct ortho_zebrafish_gene_zdb_id), "Genes with Human Orthology"
 from ortholog_evidence, organism, ortholog
  where organism_common_name like "%Human%"
 and organism_taxid = ortho_other_species_taxid
 and ortho_Zdb_id = oev_ortho_Zdb_id
;


--Genes w/Mouse Orthology
insert into tmp_output(counter, section)
select count( distinct ortho_zebrafish_gene_zdb_id), "Genes with Mouse Orthology"
 from ortholog_evidence, organism, ortholog
  where organism_common_name like "%Mouse%"
 and organism_taxid = ortho_other_species_taxid
 and ortho_Zdb_id = oev_ortho_Zdb_id
;

unload to stats.txt
select * from tmp_output;

commit work ;