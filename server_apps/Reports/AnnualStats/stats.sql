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
select count(*) gene from marker
 where mrkr_type[1,4] = 'GENE'
--and mrkr_zdb_id not like "ZDB-%-12____-%"
;
-- Genes on Vega Assembly
select count(distinct mrel_mrkr_1_zdb_id) gene_on_assembly
 from marker_relationship
 where mrel_type == 'gene produces transcript'
;

-- Transcripts
select count(*) vega_tscript from db_link
 where dblink_acc_num[1,8] = 'OTTDART0'
;
--
--grep OTTDART0 db_link | wc -l

-- EST/cDNAs
select count(*) est_cdna from marker
 where mrkr_type in ('EST','CDNA')
--and mrkr_zdb_id not like 'ZDB-%-12____-%'
;

--------------------------------------------------------------------- Genetics----------------------
-- Features          (Alleles)
select count(*) feature from feature --alteration -- fish
--where feature_zdb_id not like 'ZDB-%-12____-%'
;

--  Transgenic Features
select count(*) tg_feat
from feature
where feature_type = 'TRANSGENIC_INSERTION'
  and feature_name not like "%;%"
;

--  Transgenic Construct
select count(distinct mrkr_zdb_id) tg_const
from marker
where mrkr_type = 'TGCONSTRCT'
  and mrkr_abbrev not like "%;%"
;


--  Transgenic Genotypes
select  count(distinct genofeat_geno_zdb_id)  tg_geno
 from genotype_feature, feature
 where genofeat_feature_zdb_id = feature_zdb_id
   and feature_type = 'TRANSGENIC_INSERTION'
;

-- Genotypes (non-Wildtype)
select count(*) geno from genotype where  geno_is_wildtype == 'f'
;


-- Genes with GO annotation
select count(distinct mrkrgoev_mrkr_zdb_id) go_gene from marker_go_term_evidence
-- cut -f 2 -d\| < marker_go_term_evidence | sort -u | wc -l
;

-- IEA GO annotations
select count(distinct mrkrgoev_mrkr_zdb_id) go_iea from marker_go_term_evidence
where mrkrgoev_evidence_code = 'IEA'
-- cut -f 2,5 -d\| < marker_go_term_evidence | grep "|IEA" |sort -u | wc -l

;

-- Non-IEA GO annotations
select count(distinct mrkrgoev_mrkr_zdb_id) go_niea from marker_go_term_evidence
where mrkrgoev_evidence_code != 'IEA'
-- cut -f 2,5 -d\| < marker_go_term_evidence | grep -v "IEA" | cut -f1 -d \|| sort -u | wc -l

;

-- Total GO annotations
select count(*) go_all from marker_go_term_evidence
-- wc -l marker_go_term_evidence

;


---------------------------------------------------------------Reagents ----------------------------
--Morpholinos
select count(*) mo from marker
 where mrkr_zdb_id[1,12] = 'ZDB-MRPHLNO-'
--and mrkr_zdb_id not like 'ZDB-%-12____-%'
; --grep 'ZDB-MRPHLNO-' marker | wc -l

-- Antibodies
select count(*) ab from marker
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
select count(*) exp_pat from expression_experiment
--
where xpatex_zdb_id not like 'ZDB-%-10____-%'
;

-- Images (phenotypes, expression patterns)
select count(*)fish_img from image --fish_image
--where img_zdb_id not like 'ZDB-%-12____-%'
;

-- Anatomical structures
select count(*)anatomy_item from anatomy_item
--where anatitem_zdb_id not like 'ZDB-%-12____-%'
;

-- Developmental stages select count(*) stage from stage
--where stg_zdb_id not like 'ZDB-%-12____-%';
------------------------------Genomics------------------------------------------
-- Mapping panels select count(*) panel from panels
--where zdb_id not like 'ZDB-%-12____-%';

-- Mapped markers
select count(*) map_mrkr from paneled_markers
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
select count(*) db_links from db_link, foreign_db_contains,foreign_db
 where fdbcont_fdb_db_id == fdb_db_pk_id
   and  dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and  fdb_db_name in (
                       --2011-12-16
------------------------- ---------
"Curated miRNA Mature", --                  41
"Curated miRNA Stem Loop", --                3
"EBI-Cell", --                             776
"EC", --                                   858
"Ensembl(Zv9)", --                       16279
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
"TAO", --                                 4717
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
select count(*) pub from publication
--where zdb_id not like 'ZDB-%-12____-%'
;

-- Researchers
select count(*) person from person
--where zdb_id not like 'ZDB-%-12____-%'
;

-- Laboratories
select count(*)lab from lab
--where zdb_id not like 'ZDB-%-12____-%'
;

-- Companies
select count(*)company from company
--where zdb_id not like 'ZDB-%-12____-%'
;

---------------------------------------------------------------- Orthology ---------------------

--Genes w/Human Orthology
select count( distinct oevdisp_gene_zdb_id) human
 from orthologue_evidence_display
  where oevdisp_organism_list like "%Human%"
;


--Genes w/Mouse Orthology
select count( distinct oevdisp_gene_zdb_id) mouse
 from orthologue_evidence_display
  where oevdisp_organism_list like "%Mouse%"
;

