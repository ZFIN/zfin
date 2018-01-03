begin work ;

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
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Genes", "Genes", current year to second from marker
 where mrkr_type[1,4] = 'GENE' or mrkr_type like '%RNAG%'
;
-- Genes on Vega Assembly
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct mrel_mrkr_1_zdb_id), "Genes", "Genes on Assembly", current year to second
 from marker_relationship
 where mrel_type == 'gene produces transcript'
;

-- Transcripts
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Genes", "Transcripts", current year to second from db_link
 where dblink_acc_num[1,8] = 'OTTDART0'
;
--
--grep OTTDART0 db_link | wc -l

-- EST/cDNAs
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Genes", "EST/cDNAs", current year to second from marker
 where mrkr_type in ('EST','CDNA')
;

--------------------------------------------------------------------- Genetics----------------------
-- Features          (Alleles)
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Genetics", "Features", current year to second from feature --alteration -- fish
;

--  Transgenic Features
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Genetics", "Transgenic Features", current year to second
from feature
where feature_type = 'TRANSGENIC_INSERTION'
  and feature_name not like "%;%"
;

--  Transgenic Construct
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct mrkr_zdb_id), "Genetics", "Transgenic Constructs", current year to second
from marker
where mrkr_type in  ('TGCONSTRCT','PTCONSTRCT','GTCONSTRCT','ETCONSTRCT')
  and mrkr_abbrev not like "%;%"
;


--  Transgenic Genotypes
insert into annual_stats(as_count, as_section, as_type, as_date)
select  count(distinct genofeat_geno_zdb_id), "Genetics", "Transgenic Genotypes", current year to second
 from genotype_feature, feature
 where genofeat_feature_zdb_id = feature_zdb_id
   and feature_type = 'TRANSGENIC_INSERTION'
;

-- Genotypes (non-Wildtype)
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Genetics", "Genotypes", current year to second
 from genotype where  geno_is_wildtype == 'f'
;


-- Genes with GO annotation
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct mrkrgoev_mrkr_zdb_id), "Genetics", "Genes with GO annotations", current year to second from marker_go_term_evidence
;

-- IEA GO annotations
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct mrkrgoev_mrkr_zdb_id), "Genetics", "Genes with IEA GO annotations", current year to second from marker_go_term_evidence
where mrkrgoev_evidence_code = 'IEA'

;

-- Non-IEA GO annotations
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct mrkrgoev_mrkr_zdb_id), "Genetics", "Genes with Non-IEA GO anotations", current year to second 
from marker_go_term_evidence
where mrkrgoev_evidence_code != 'IEA'

;

-- Total GO annotations
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Genetics", "Total GO Annotations", current year to second from marker_go_term_evidence

;

--Genes with OMIM disease phenotypes
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct ortho_zebrafish_gene_zdb_id), "Genetics", "Genes with OMIM phenotypes", current year to second
 from ortholog where exists(select "x" from omim_phenotype where omimp_ortho_zdb_id = ortho_zdb_id);

---------------------------------------------------------------Reagents ----------------------------
--Morpholinos
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Reagents", "Morpholinos", current year to second from marker
 where mrkr_zdb_id[1,12] = 'ZDB-MRPHLNO-'
; 

--TALEN
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Reagents", "TALEN", current year to second from marker
 where mrkr_zdb_id like 'ZDB-TALEN%';

--CRISPR
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Reagents", "CRISPR", current year to second from marker
  where mrkr_zdb_id like 'ZDB-CRISPR%';

-- Antibodies
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Reagents", "Antibodies", current year to second from marker
 where mrkr_zdb_id[1,8] = 'ZDB-ATB-'
;


------------------------------------------Expression & Phenotypes---------------------------------
-- Gene expression patterns
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Expression & Phenotype", "Gene expression patterns", current year to second from expression_result;

-- clean Gene expression patterns
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct xpatex_gene_zdb_id), "Expression & Phenotype", "Genes with expression data", current year to second from expression_experiment2, clean_expression_fast_search
  where xpatex_genox_zdb_id = cefs_genox_zdb_id;


--Phenotype Statements
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Expression & Phenotype", "Phenotype statements", current year to second from phenotype_observation_generated;


--Clean phenotype
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(distinct (mfs_mrkr_Zdb_id)), "Expression & Phenotype", "Genes with a phenotype", current year to second from phenotype_statement, phenotype_Experiment, mutant_fast_search
 where phenox_pk_id = phenos_phenox_pk_id 
 and phenox_genox_zdb_id = mfs_genox_zdb_id
and (mfs_mrkr_zdb_id like 'ZDB-GENE%' or mfs_mrkr_zdb_id like '%RNAG%');

-- Images annotated for expression
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Expression & Phenotype", "Images", current year to second from image
;


-- Anatomical structures
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Expression & Phenotype", "Anatomical structures", current year to second from term
 where term_ontology in ('zebrafish_anatomy','zebrafish_anatomical_ontology');


-- Mapped markers
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Genomics", "Mapped markers", current year to second from paneled_markers
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
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Genomics", "Links to other databases", current year to second from db_link, foreign_db_contains,foreign_db
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
insert into annual_stats(as_count, as_section, as_type, as_date)

select count(*), "Community information", "All Publications", current year to second
from publication;

insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Community information", "Journal Publications", current year to second
from publication
where jtype = 'Journal';

-- Researchers
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Community information", "Researchers", current year to second from person

;

-- Laboratories
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Community information", "Laboratories", current year to second from lab

;

-- Companies
insert into annual_stats(as_count, as_section, as_type, as_date)
select count(*), "Community information", "Companies", current year to second from company

;

---------------------------------------------------------------- Orthology ---------------------

--Genes w/Human Orthology
insert into annual_stats(as_count, as_section, as_type, as_date)
select count( distinct ortho_zebrafish_gene_zdb_id), "Orthology", "Genes w/Human Orthology", current year to second
 from ortholog_evidence, organism, ortholog
  where organism_common_name like "%Human%"
 and organism_taxid = ortho_other_species_taxid
 and ortho_Zdb_id = oev_ortho_Zdb_id
;


--Genes w/Mouse Orthology
insert into annual_stats(as_count, as_section, as_type, as_date)
select count( distinct ortho_zebrafish_gene_zdb_id), "Orthology", "Genes w/Mouse Orthology", current year to second
 from ortholog_evidence, organism, ortholog
  where organism_common_name like "%Mouse%"
 and organism_taxid = ortho_other_species_taxid
 and ortho_Zdb_id = oev_ortho_Zdb_id
;

unload to stats.txt
select as_date, as_pk_id, as_section, as_type, as_count from annual_stats
 order by as_date desc, as_pk_id asc;

commit work ;
