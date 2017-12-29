-- Script to create data files for public download.
--
-- We extract several different kinds of information:
--
-- All genetic markers (includes genes, ESTs, SSLPs, etc.)
--	zfin id, name, symbol, type, OR_lg
--
-- Synonyms  (for any item in all genetic markers file) There may be multiple lines
--   per zfin id
--	zfin id, synonym
--
-- Orthology - separate files for: D
--   zebrafish - human
--	zfin id , zebrafish symbol, human symbol, OMIM id, Gene id
--   zebrafish - mouse
--	zfin id , zebrafish symbol, mouse symbol, MGI id, Gene id
--   zebrafish - fly
--	zfin id,  zebrafish symbol, fly symbol,  Flybase id
--   zebrafish - yeast [5 stale records]
--	zfin id,  zebrafish symbol, yeast symbol,  SGD id
--
-- Gene Ontology
--	A copy of the file we send to GO.
--
-- Gene Expression
--	gene zfin id , gene symbol, probe zfin id, probe name, expression type,
--      expression pattern zfin id, pub zfin id, genotype zfin id,
--      experiment zfin id
--
-- Mapping data
--	zfin id, symbol, panel symbol, LG, loc, metric
--
-- Sequence data - separate files for GenBank, RefSeq, Gene, Unigene,
-- UniProt, Interpro, GenPept and Vega (genes and transcripts) 1:1 Ensembl ID
-- as well as sequences indirectly associated with genes
--	zfin id, symbol, accession number
--
-- Genotypes
--	zfin id, allele/construct, type, gene symbol, corresponding zfin gene id
--
-- create genetic markers file
--
-- Morpholino data
--      zfin id of gene, gene symbol, zfin id of MO, MO symbol, public note
-- Marker Relationship data
--	marker1 id, marker1 symbol, marker 2 id, marker 2 symbol, relationship

-- prepare phenotype data:
-- add term_ont_ids and names and post-composed relationships
begin work;

--! echo "Create table to hold phenotype data"
create temp table tmp_phenotype_statement (
       phenos_pk_id serial,
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
  select psg_id, asubterm.term_ont_id, asubterm.term_name, asuperterm.term_ont_id, asuperterm.term_name,
         bsubterm.term_ont_id, bsubterm.term_name, bsuperterm.term_ont_id, bsuperterm.term_name,
         quality.term_ont_id, quality.term_name,  asubterm.term_ontology, bsubterm.term_ontology, psg_tag
    from phenotype_observation_generated
         full outer join term as asubterm on asubterm.term_zdb_id = psg_e1b_zdb_id
         full outer join term as asuperterm on asuperterm.term_zdb_id = psg_e1a_zdb_id
         full outer join term as bsubterm on bsubterm.term_zdb_id = psg_e2b_zdb_id
         full outer join term as bsuperterm on bsuperterm.term_zdb_id = psg_e2a_zdb_id
         join term as quality on quality.term_zdb_id = psg_quality_zdb_id;
          
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

-- create antibody download file
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/antibodies2.txt'"
create view antibodies as
select mrkr_zdb_id, mrkr_abbrev, atb_type, atb_hviso_name, atb_ltiso_name,
	atb_immun_organism, atb_host_organism, szm_term_ont_id
  from marker, antibody, so_zfin_mapping
 where mrkr_zdb_id = atb_zdb_id
 and szm_object_type = mrkr_type
 order by 1;
\copy (select * from antibodies) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/antibodies2.txt' with delimiter as '	' null as '';
drop view antibodies;

-- create antibody expression download file
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/antibody_expressions_fish.txt'"
create view antibodyExpression as
select  mrkr_zdb_id, super.term_ont_id, super.term_name, sub.term_ont_id as subontid, sub.term_name as subname
 from marker, expression_experiment, expression_result, fish, term as super
      , fish_experiment, genotype, term sub
 where xpatres_xpatex_zdb_id = xpatex_zdb_id
   AND xpatex_atb_zdb_id = mrkr_zdb_id
   AND mrkr_type = 'ATB'
   AND super.term_zdb_id = xpatres_superterm_zdb_id
   AND sub.term_zdb_id = xpatres_subterm_zdb_id
   AND xpatex_genox_zdb_id = genox_zdb_id
   AND genox_is_std_or_generic_control = 't'
   AND xpatres_expression_found = 't'
   AND fish_zdb_id = genox_fish_zdb_id
   AND geno_zdb_id = fish_genotype_zdb_id
   AND geno_is_wildtype = 't'
   and not exists ( select 'x' from clone where clone_mrkr_Zdb_id = xpatex_probe_Feature_zdb_id
       	   	    	       	    	  and clone_problem_type = 'Chimeric')
   and fish_functional_Affected_gene_count = 0
 and not exists (Select 'x' from genotype_feature, feature_marker_relationship
      	  	 	 where geno_zdb_id = genofeat_geno_zdb_id
			 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
			 and fmrel_type = 'contains phenotypic sequence feature')
  and xpatres_subterm_zdb_id is not null
union
select  mrkr_zdb_id, super.term_ont_id, super.term_name, '' as subontid, '' as subname
 from marker, expression_experiment, expression_result, fish, term as super
      , fish_experiment, genotype
 where xpatres_xpatex_zdb_id = xpatex_zdb_id
   AND xpatex_atb_zdb_id = mrkr_zdb_id
   AND mrkr_type = 'ATB'
   AND super.term_zdb_id = xpatres_superterm_zdb_id
   AND xpatex_genox_zdb_id = genox_zdb_id
   AND genox_is_std_or_generic_control = 't'
   AND xpatres_expression_found = 't'
   AND fish_zdb_id = genox_fish_zdb_id
   AND geno_zdb_id = fish_genotype_zdb_id
   AND geno_is_wildtype = 't'
   and not exists ( select 'x' from clone where clone_mrkr_Zdb_id = xpatex_probe_Feature_zdb_id
       	   	    	       	    	  and clone_problem_type = 'Chimeric')
 and fish_functional_Affected_gene_count = 0
  and not exists (Select 'x' from genotype_feature, feature_marker_relationship
      	  	 	 where geno_zdb_id = genofeat_geno_zdb_id
			 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
			 and fmrel_type = 'contains phenotypic sequence feature')
  and xpatres_subterm_zdb_id is null
 order by mrkr_zdb_id
;

\copy (select * from antibodyExpression) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/antibody_expressions_fish.txt' with delimiter as '	' null as '';
drop view antibodyExpression;

-- create all marker file
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genetic_markers.txt'"

create view geneticMarkers as
select mrkr_zdb_id, mrkr_abbrev, mrkr_name, mrkr_type, szm_term_ont_id
 from marker, so_zfin_mapping
 where szm_object_type = mrkr_type
  order by 1;
\copy (select * from geneticMarkers) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genetic_markers.txt' with delimiter as '	' null as '';  
drop view geneticMarkers;  
  
-- create other names file

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/aliases.txt'"
create view aliasView as  
select mrkr_zdb_id , mrkr_name, mrkr_abbrev, dalias_alias, szm_term_ont_id
 from marker, data_alias, so_zfin_mapping
 where dalias_data_zdb_id = mrkr_zdb_id
and szm_object_type = mrkr_type
union
select feature_zdb_id, feature_name,feature_abbrev, dalias_alias, szm_term_ont_id
 from feature, data_alias, so_zfin_mapping
 where feature_zdb_id = dalias_data_zdb_id
and szm_object_type = feature_type
union
select geno_zdb_id, geno_display_name, geno_handle, dalias_alias, szm_term_ont_id
 from genotype, data_alias, so_zfin_mapping
 where dalias_data_zdb_id = geno_Zdb_id
 and szm_object_type = 'GENOTYPE'
 order by 1, 4
;
\copy (select * from aliasView) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/aliases.txt' with delimiter as '	' null as '';
drop view aliasView;

-- Create marker realtionship file
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/gene_marker_relationship.txt'"

create view gene_marker_relationship as
select gene.mrkr_zdb_id, a.szm_term_ont_id, gene.mrkr_abbrev, seq.mrkr_zdb_id as mrkrId2, b.szm_term_ont_id as ontId2, seq.mrkr_abbrev as symbol2, mrel_type
 from marker_relationship, marker as gene, marker as seq, so_zfin_mapping as a, so_zfin_mapping as b, marker_type_group_member as mem
 where gene.mrkr_type=mem.mtgrpmem_mrkr_type
  and mem.mtgrpmem_mrkr_type_group='GENEDOM'
   and seq.mrkr_type not in ('GENE', 'GENEP')
   and mrel_mrkr_1_zdb_id = gene.mrkr_zdb_id
   and mrel_mrkr_2_zdb_id = seq.mrkr_zdb_id
   and a.szm_object_type = gene.mrkr_type
   and b.szm_object_type = seq.mrkr_type
union
select gene.mrkr_zdb_id, a.szm_term_ont_id, gene.mrkr_abbrev, seq.mrkr_zdb_id as mrkrId2,  b.szm_term_ont_id as ontId2, seq.mrkr_abbrev as symbol2, mrel_type
 from marker_relationship, marker as gene, marker as seq, so_zfin_mapping as a, so_zfin_mapping as b, marker_type_group_member as mem
 where gene.mrkr_type=mem.mtgrpmem_mrkr_type
  and mem.mtgrpmem_mrkr_type_group='GENEDOM'
   and seq.mrkr_type not in ('GENE', 'GENEP')
   and mrel_mrkr_2_zdb_id = gene.mrkr_zdb_id
   and mrel_mrkr_1_zdb_id = seq.mrkr_zdb_id
   and a.szm_object_type = gene.mrkr_type
   and b.szm_object_type = seq.mrkr_type
;

\copy (select * from gene_marker_relationship) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/gene_marker_relationship.txt' with delimiter as '	' null as '';
drop view gene_marker_relationship;

-- ==== BEGIN ORTHOLOGY QUERIES ====
CREATE TEMP TABLE tmp_hgnc (
  ortho_id text,
  accession text
);
INSERT INTO tmp_hgnc
  SELECT oef_ortho_zdb_id, oef_accession_number
  FROM ortholog_external_reference
  INNER JOIN foreign_db_contains ON oef_fdbcont_zdb_id = fdbcont_zdb_id
  INNER JOIN foreign_db ON fdb_db_pk_id = fdbcont_fdb_db_id
  WHERE fdb_db_name = 'HGNC';

CREATE TEMP TABLE tmp_omim (
  ortho_id text,
  accession text
);
INSERT INTO tmp_omim
  SELECT oef_ortho_zdb_id, oef_accession_number
  FROM ortholog_external_reference
  INNER JOIN foreign_db_contains ON oef_fdbcont_zdb_id = fdbcont_zdb_id
  INNER JOIN foreign_db ON fdb_db_pk_id = fdbcont_fdb_db_id
  WHERE fdb_db_name = 'OMIM';

CREATE TEMP TABLE tmp_gene (
  ortho_id text,
  accession text
);
INSERT INTO tmp_gene
  SELECT oef_ortho_zdb_id, oef_accession_number
  FROM ortholog_external_reference
  INNER JOIN foreign_db_contains ON oef_fdbcont_zdb_id = fdbcont_zdb_id
  INNER JOIN foreign_db ON fdb_db_pk_id = fdbcont_fdb_db_id
  WHERE fdb_db_name = 'Gene';

CREATE TEMP TABLE tmp_mgi (
  ortho_id text,
  accession text
);
INSERT INTO tmp_mgi
  SELECT oef_ortho_zdb_id, oef_accession_number
  FROM ortholog_external_reference
  INNER JOIN foreign_db_contains ON oef_fdbcont_zdb_id = fdbcont_zdb_id
  INNER JOIN foreign_db ON fdb_db_pk_id = fdbcont_fdb_db_id
  WHERE fdb_db_name = 'MGI';

CREATE TEMP TABLE tmp_flybase (
  ortho_id text,
  accession text
);
INSERT INTO tmp_flybase
  SELECT oef_ortho_zdb_id, oef_accession_number
  FROM ortholog_external_reference
  INNER JOIN foreign_db_contains ON oef_fdbcont_zdb_id = fdbcont_zdb_id
  INNER JOIN foreign_db ON fdb_db_pk_id = fdbcont_fdb_db_id
  WHERE fdb_db_name = 'FLYBASE';
  
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/human_orthos.txt'"
create view human_orthos as
  SELECT DISTINCT mrkr_zdb_id, mrkr_abbrev, mrkr_name, ortho_other_species_symbol, ortho_other_species_name,
                  tmp_omim.accession, tmp_gene.accession as accession2, tmp_hgnc.accession as accession3, oev_evidence_code, oev_pub_zdb_id
    FROM ortholog
    INNER JOIN marker ON ortho_zebrafish_gene_zdb_id = mrkr_zdb_id
    INNER JOIN ortholog_evidence ON ortho_zdb_id = oev_ortho_zdb_id
    INNER JOIN organism ON ortho_other_species_taxid = organism_taxid
    LEFT OUTER JOIN tmp_omim ON ortho_zdb_id = tmp_omim.ortho_id
    LEFT OUTER JOIN tmp_gene ON ortho_zdb_id = tmp_gene.ortho_id
    LEFT OUTER JOIN tmp_hgnc ON ortho_zdb_id = tmp_hgnc.ortho_id
    WHERE organism_common_name = 'Human'
    ORDER BY mrkr_zdb_id;
\copy (select * from human_orthos) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/human_orthos.txt' with delimiter as '	' null as '';  
drop view human_orthos;    

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/fly_orthos.txt'"
create view fly_orthos as
  SELECT DISTINCT mrkr_zdb_id, mrkr_abbrev, mrkr_name, ortho_other_species_symbol, ortho_other_species_name,
                  tmp_flybase.accession, tmp_gene.accession as accession2, oev_evidence_code, oev_pub_zdb_id
    FROM ortholog
    INNER JOIN marker ON ortho_zebrafish_gene_zdb_id = mrkr_zdb_id
    INNER JOIN ortholog_evidence ON ortho_zdb_id = oev_ortho_zdb_id
    INNER JOIN organism ON ortho_other_species_taxid = organism_taxid
    LEFT OUTER JOIN tmp_flybase ON ortho_zdb_id = tmp_flybase.ortho_id
    LEFT OUTER JOIN tmp_gene ON ortho_zdb_id = tmp_gene.ortho_id
    WHERE organism_common_name = 'Fruit fly'
    ORDER BY mrkr_zdb_id;
\copy (select * from fly_orthos) TO '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/fly_orthos.txt' with delimiter as '	' null as '';
drop view fly_orthos;    

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/mouse_orthos.txt'"
create view mouse_orthos as
  SELECT DISTINCT mrkr_zdb_id, mrkr_abbrev, mrkr_name, ortho_other_species_symbol, ortho_other_species_name,
                  'MGI:' || tmp_mgi.accession, tmp_gene.accession as accession2, oev_evidence_code, oev_pub_zdb_id
    FROM ortholog
    INNER JOIN marker ON ortho_zebrafish_gene_zdb_id = mrkr_zdb_id
    INNER JOIN ortholog_evidence ON ortho_zdb_id = oev_ortho_zdb_id
    INNER JOIN organism ON ortho_other_species_taxid = organism_taxid
    LEFT OUTER JOIN tmp_mgi ON ortho_zdb_id = tmp_mgi.ortho_id
    LEFT OUTER JOIN tmp_gene ON ortho_zdb_id = tmp_gene.ortho_id
    WHERE organism_common_name = 'Mouse'
    ORDER BY mrkr_zdb_id;
\copy (select * from mouse_orthos) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/mouse_orthos.txt' with delimiter as '	' null as '';
drop view mouse_orthos;
  
DROP TABLE tmp_omim;
DROP TABLE tmp_gene;
DROP TABLE tmp_hgnc;
DROP TABLE tmp_mgi;
DROP TABLE tmp_flybase;
-- ==== END ORTHOLOGY QUERIES ====  

-- generate a file with genes and associated expression experiment
create temp table tmp_xpat_Fish (gene_zdb_id text,
                                 gene_abbrev text,
                                 probe_zdb_id text,
                                 probe_abbrev text,
                                 xpatex_assay_name text,
                                 xpat_zdb_id text,
                                 source_zdb_id text,
                                 fish_zdb_id text,
                                 xpatex_zdb_id text,
                                 clone_rating text);


insert into tmp_xpat_fish (gene_zdb_id,
                                gene_abbrev,
                                probe_zdb_id,
                                probe_abbrev,
                                xpatex_assay_name,
                                xpat_zdb_id,
                                source_zdb_id,
                                fish_zdb_id,
                                xpatex_zdb_id)
select gene.mrkr_zdb_id gene_zdb, gene.mrkr_abbrev,
        probe.mrkr_zdb_id as probe_zdb, probe.mrkr_abbrev as probe_abbr,
        xpatex_assay_name, xpatex_zdb_id xpat_zdb,
        xpatex_source_zdb_id,
        fish.fish_zdb_id, genox_exp_zdb_id
 from expression_experiment
   join fish_experiment on genox_zdb_id = xpatex_genox_zdb_id  
   join marker as gene on gene.mrkr_zdb_id = xpatex_gene_zdb_id
   join fish as fish on fish.fish_zdb_id = genox_fish_zdb_id
   full outer join marker as probe on probe.mrkr_zdb_id = xpatex_probe_feature_zdb_id
   where substring(gene.mrkr_abbrev from 1 for 10) != 'WITHDRAWN:'
   and exists (
        select 1 from expression_result
         where xpatres_xpatex_zdb_id = xpatex_zdb_id
 ) order by gene_zdb, xpat_zdb, probe_zdb;

delete from tmp_xpat_fish
 where exists (Select 'x' from clone
                      where clone_mrkr_Zdb_id = probe_zdb_id
                      and clone_problem_type = 'Chimeric');

update tmp_xpat_fish
 set clone_rating = (select clone_rating from clone where clone_mrkr_zdb_id = probe_zdb_id);

 
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpat_fish.txt'"
\copy (select * from tmp_xpat_fish) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpat_fish.txt' with delimiter as '	' null as ''; 

-- generate a file to map experiment id to environment condition description
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpat_environment_fish.txt'"

create view xpat_environment_fish as
select exp_zdb_id, zeco.term_name, zeco.term_ont_id, chebi.term_name as name2, chebi.term_ont_id as id2,
       zfa.term_name as name3, zfa.term_ont_id as id3, gocc.term_name as name4, gocc.term_ont_id as id4,
       taxon.term_name as name5, taxon.term_ont_id as id5
  from experiment_condition
join experiment on exp_zdb_id = expcond_exp_zdb_id
left outer join term zeco on zeco.term_zdb_id = expcond_zeco_term_zdb_id  
left outer join term chebi on chebi.term_zdb_id = expcond_chebi_term_zdb_id
left outer join term zfa on zfa.term_zdb_id = expcond_ao_term_zdb_id
left outer join term gocc on gocc.term_zdb_id = expcond_go_cc_term_zdb_id
left outer join term taxon on taxon.term_zdb_id = expcond_taxon_term_zdb_id
 where exists (
        select 'x' from fish_experiment, expression_experiment
         where expcond_exp_zdb_id = genox_exp_zdb_id
           and genox_zdb_id = xpatex_genox_zdb_id)
union
select exp_zdb_id, exp_name, '', '', '', '', '', '', '', '', ''
 from experiment
 where exp_name = '_Generic-control'  
union
select exp_zdb_id, 'standard environment', '', '', '', '', '', '', '', '', ''
 from experiment
 where not exists (Select 'x' from experiment_condition
                          where exp_zdb_id = expcond_exp_zdb_id)
   and exists (
        select 't' from fish_experiment, expression_experiment
         where exp_zdb_id = genox_exp_zdb_id
           and genox_zdb_id = xpatex_genox_zdb_id)
 order by 1, 2, 4
;
\copy (select * from xpat_environment_fish) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpat_environment_fish.txt' with delimiter as '	' null as '';
drop view xpat_environment_fish;

-- generate a file with genes and associated expression experiment
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/phenotype_fish.txt'"

create view phenotype_fish as
 select distinct f.fish_zdb_id, f.fish_full_name,
            pg_start_stg_zdb_id,
            (select stg_name
                from stage
                where stg_zdb_id = pg_start_stg_zdb_id),
            pg_end_stg_zdb_id,
            (select stg_name as name2 from stage where stg_zdb_id = pg_end_stg_zdb_id),
              tps.asubterm_ont_id,
              tps.asubterm_name,
              tps.arelationship_id,
              tps.arelationship_name,
              tps.asuperterm_ont_id,
              tps.asuperterm_name,
              tps.quality_id,
              tps.quality_name,
              psg_tag,
              tps.bsubterm_ont_id,
              tps.bsubterm_name,
              tps.brelationship_id,
              tps.brelationship_name,
              tps.bsuperterm_ont_id,
              tps.bsuperterm_name,
              fig_source_zdb_id,
              gx.genox_exp_zdb_id
  from phenotype_source_generated, phenotype_observation_generated ps, figure, fish f, fish_experiment gx, tmp_phenotype_statement tps
 where ps.psg_pg_id = pg_id
   and pg_genox_zdb_id = gx.genox_zdb_id
   and f.fish_zdb_id = gx.genox_fish_zdb_id
   and pg_fig_zdb_id = fig_zdb_id
   and ps.psg_id = tps.phenos_pk_id
 order by fish_zdb_id, fig_source_zdb_id;
\copy (select * from phenotype_fish) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/phenotype_fish.txt' with delimiter as '	' null as '';
drop view phenotype_fish;

-- generate a file with xpatex and associated figure zdbid's
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpatfig_fish.txt'"
create view xpatfig_fish as
select distinct xpatex_zdb_id, xpatres_zdb_id, xpatfig_fig_zdb_id
 from expression_experiment, expression_result,expression_pattern_figure
 where xpatex_zdb_id=xpatres_xpatex_zdb_id
   and xpatres_zdb_id=xpatfig_xpatres_zdb_id
 and not exists (select 'x' from clone
     	 	where clone_problem_type = 'Chimeric'
		and clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id)
 order by xpatex_zdb_id;
\copy (select * from xpatfig_fish) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpatfig_fish.txt' with delimiter as '	' null as '';
drop view xpatfig_fish;

-- generate a file with genotype id's and associated figure zdbid's
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genofig_fish.txt'"
create view genofig_fish as
 select distinct genox_fish_zdb_id, pg_fig_zdb_id
 from fish_experiment, phenotype_source_generated
 where genox_zdb_id = pg_genox_zdb_id
 order by genox_fish_zdb_id;
\copy (select * from genofig_fish) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genofig_fish.txt' with delimiter as '	' null as '';
drop view genofig_fish;

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/pheno_environment_fish.txt'"

create view pheno_environment_fish as
select exp_zdb_id, zeco.term_name, zeco.term_ont_id, chebi.term_name as name2, chebi.term_ont_id as id2,
       zfa.term_name as name3, zfa.term_ont_id as id3, gocc.term_name as name4, gocc.term_ont_id as id4,
       taxon.term_name as name5, taxon.term_ont_id as id5  
  from experiment_condition
join experiment on exp_zdb_id = expcond_exp_zdb_id
left outer join term zeco on zeco.term_zdb_id = expcond_zeco_term_zdb_id  
left outer join term chebi on chebi.term_zdb_id = expcond_chebi_term_zdb_id
left outer join term zfa on zfa.term_zdb_id = expcond_ao_term_zdb_id
left outer join term gocc on gocc.term_zdb_id = expcond_go_cc_term_zdb_id
left outer join term taxon on taxon.term_zdb_id = expcond_taxon_term_zdb_id
 where exists (
        select 't'
          from fish_experiment, phenotype_source_generated
         where exp_zdb_id = genox_exp_zdb_id
           and genox_zdb_id = pg_genox_zdb_id) 
order by 1, 2, 4
;
\copy (select * from pheno_environment_fish) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/pheno_environment_fish.txt' with delimiter as '	' null as '';
drop view pheno_environment_fish;

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/gene_expression_phenotype.txt'"

create view gene_expression_phenotype as
select distinct (select mrkr_abbrev from marker where mrkr_zdb_id = psg_mrkr_zdb_id) as gene,
                psg_mrkr_zdb_id, 
                'expressed in' as expressedIn,
                'RO:0002206' as ro,
                psg_e1a_name,
                (select term_ont_id from term where term_zdb_id = psg_e1a_zdb_id) as id1,
                psg_e1b_name,
                (select term_ont_id from term where term_zdb_id = psg_e1b_zdb_id) as id2,
                psg_quality_name,
                (select term_ont_id from term where term_zdb_id = psg_quality_zdb_id) as id3,
                psg_tag,
                (select stg_name from stage where stg_zdb_id = pg_start_stg_zdb_id) as stg1,
                pg_start_stg_zdb_id,
                (select stg_name from stage where stg_zdb_id = pg_end_stg_zdb_id) as stg2,
                pg_end_stg_zdb_id,
                xpatex_assay_name,
                xpatex_probe_feature_zdb_id,
                (select mrkr_abbrev from marker where mrkr_zdb_id = xpatex_atb_zdb_id) as symbol,
                xpatex_atb_zdb_id,
                genox_fish_zdb_id,
                genox_exp_zdb_id,
                pg_fig_zdb_id as figure,
                fig_source_zdb_id as publication,
                pub.accession_no
from phenotype_observation_generated, phenotype_source_generated, expression_experiment2, expression_figure_stage, expression_result2, fish_experiment, figure, publication pub
where substring(psg_mrkr_zdb_id from 1 for 8) in ('ZDB-GENE', 'ZDB-EFG-', 'ZDB-LNCR','ZDB-LINC','ZDB-TRNA','ZDB-RRNA','ZDB-MIRN','ZDB-SNOR','ZDB-NCRN','ZDB-PIRN')
  and psg_pg_id = pg_id
  and xpatex_genox_zdb_id = pg_genox_zdb_id
  and xpatex_gene_zdb_id = psg_mrkr_zdb_id
  and efs_xpatex_zdb_id = xpatex_zdb_id
  and efs_fig_zdb_id = pg_fig_zdb_id
  and efs_start_stg_zdb_id = pg_start_stg_zdb_id
  and efs_end_stg_zdb_id = pg_end_stg_zdb_id
  and xpatres_efs_id = efs_pk_id
  and xpatres_superterm_zdb_id = psg_e1a_zdb_id
  and genox_zdb_id = pg_genox_zdb_id
  and fig_zdb_id = pg_fig_zdb_id
  and pub.zdb_id = fig_source_zdb_id 
 order by gene, publication, figure;
\copy (select * from gene_expression_phenotype) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/gene_expression_phenotype.txt' with delimiter as '	' null as '';
drop view gene_expression_phenotype; 

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/antibody_labeling_phenotype.txt'"

create view antibody_labeling_phenotype as
select distinct (select mrkr_name from marker where mrkr_zdb_id = psg_mrkr_zdb_id) as antibody,
                psg_mrkr_zdb_id, 
                'eptitope' as ept,
                'SO:0001018' as so,
                'expressed in' as expressedIn,
                'RO:0002206' as ro,
                psg_e1a_name,
                (select term_ont_id from term where term_zdb_id = psg_e1a_zdb_id) as id1,
                psg_e1b_name,
                (select term_ont_id from term where term_zdb_id = psg_e1b_zdb_id) as id2,
                psg_quality_name,
                (select term_ont_id from term where term_zdb_id = psg_quality_zdb_id) as id3,
                psg_tag,
                (select stg_name from stage where stg_zdb_id = pg_start_stg_zdb_id) as stg1,
                pg_start_stg_zdb_id,
                (select stg_name from stage where stg_zdb_id = pg_end_stg_zdb_id) as stg2,
                pg_end_stg_zdb_id,
                xpatex_assay_name,
                genox_fish_zdb_id,
                genox_exp_zdb_id,
                pg_fig_zdb_id as figure,
                fig_source_zdb_id as publication,
                pub.accession_no 
from phenotype_observation_generated, phenotype_source_generated, expression_experiment2, expression_figure_stage, expression_result2, fish_experiment, figure, publication pub
where substring(psg_mrkr_zdb_id from 1 for 7) = 'ZDB-ATB'
  and psg_pg_id = pg_id
  and xpatex_genox_zdb_id = pg_genox_zdb_id
  and xpatex_atb_zdb_id = psg_mrkr_zdb_id
  and efs_xpatex_zdb_id = xpatex_zdb_id
  and efs_fig_zdb_id = pg_fig_zdb_id
  and efs_start_stg_zdb_id = pg_start_stg_zdb_id
  and efs_end_stg_zdb_id = pg_end_stg_zdb_id
  and xpatres_efs_id = efs_pk_id
  and xpatres_superterm_zdb_id = psg_e1a_zdb_id
  and genox_zdb_id = pg_genox_zdb_id
  and fig_zdb_id = pg_fig_zdb_id
  and pub.zdb_id = fig_source_zdb_id
 order by antibody, publication, figure;
\copy (select * from antibody_labeling_phenotype) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/antibody_labeling_phenotype.txt' with delimiter as '	' null as '';
drop view antibody_labeling_phenotype; 

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/fishPub.txt'"

create view fishPub as 
 select zdb_id, accession_no, recattrib_data_zdb_id from publication, fish, record_attribution
 	   where recattrib_Data_zdb_id = fish_Zdb_id 
	   and recattrib_source_zdb_id = zdb_id
	   and recattrib_source_type = 'standard' ;
\copy (select * from fishPub) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/fishPub.txt' with delimiter as '	' null as '';	   
drop view fishPub;

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genoPub.txt'"

create view genoPub as 
 select zdb_id, accession_no, recattrib_data_zdb_id from publication, genotype, record_attribution
 	   where recattrib_Data_zdb_id = geno_Zdb_id 
	   and recattrib_source_zdb_id = zdb_id
	   and recattrib_source_type = 'standard' ;
\copy (select * from genoPub) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genoPub.txt' with delimiter as '	' null as '';
drop view genoPub;

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/pub_to_pubmed_id_translation.txt'"
\copy (select zdb_id, accession_no from publication) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/pub_to_pubmed_id_translation.txt' with delimiter as '	' null as '';

-- Create mapping data file
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/mappings.txt'"

create view mappings as
select marker_id, mrkr_abbrev, szm_term_ont_id, p.abbrev, mm_chromosome, mm_chrom_location, p.metric
 from mapped_marker, panels p, marker m, so_zfin_mapping
 where refcross_id = p.zdb_id and marker_id = mrkr_zdb_id
 and  m.mrkr_type = szm_object_type
union
select marker_id, feature_abbrev, szm_term_ont_id, p.abbrev, mm_chromosome, mm_chrom_location, p.metric
 from mapped_marker, panels p, feature , so_zfin_mapping
 where refcross_id = p.zdb_id and marker_id = feature_zdb_id
 and  feature_type = szm_object_type
union
select paneled_markers.zdb_id, mrkr_Abbrev, szm_term_ont_id, p.abbrev,or_lg, lg_location, p.metric
 from paneled_markers, panels p, marker , so_zfin_mapping
 where target_id = p.zdb_id and paneled_markers.zdb_id = mrkr_zdb_id
 and  mrkr_type = szm_object_type
order by 1;
\copy (select * from mappings) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/mappings.txt' with delimiter as '	' null as '';
drop view mappings;

-- Generate sequence data files for GenBank, RefSeq, Entrez, UniGene, UniProt, Interpro and GenPept

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genbank.txt'"

create view genbank as
select mrkr_zdb_id, szm_term_ont_id, mrkr_abbrev, dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db, so_zfin_mapping
 where mrkr_zdb_id = dblink_linked_recid
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and szm_object_type = mrkr_type
   and fdb_db_name = 'GenBank'
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and substring(dblink_acc_num from 3 for 1) != '_'  -- filter misplaced acc
   and not (
   	substring(dblink_linked_recid from 1 for 12) = 'ZDB-TSCRIPT-'
   	and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37'
 ) order by 1;
\copy (select * from genbank) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genbank.txt' with delimiter as '	' null as '';
drop view genbank;

-- the last condition is added to filter out mis-placed acc
--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/refseq.txt'"

create view refSeq as
select mrkr_zdb_id, szm_term_ont_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db, so_zfin_mapping
 where mrkr_zdb_id = dblink_linked_recid
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and szm_object_type = mrkr_type
   and fdb_db_name = 'RefSeq'
   and substring(dblink_acc_num from 3 for 1) = '_'
 order by 1;
\copy (select * from refSeq) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/refseq.txt' with delimiter as '	' null as ''; 
drop view refSeq; 

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/gene.txt'"

create view genes as
select mrkr_zdb_id, szm_term_ont_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db, so_zfin_mapping
 where mrkr_zdb_id = dblink_linked_recid
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and szm_object_type = mrkr_type
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdb_db_name = 'Gene'
 order by 1;
\copy (select * from genes) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/gene.txt' with delimiter as '	' null as '';
drop view genes;

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/unigene.txt'"

create view unigene as
select mrkr_zdb_id, szm_term_ont_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db, so_zfin_mapping
 where mrkr_zdb_id = dblink_linked_recid
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and szm_object_type = mrkr_type
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdb_db_name = 'UniGene'
order by 1;
\copy (select * from unigene) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/unigene.txt' with delimiter as '	' null as '';
drop view unigene;

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/uniprot.txt' with delimiter as '	' null as '';"
create view uniprot as
select mrkr_zdb_id, szm_term_ont_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db, so_zfin_mapping
 where mrkr_zdb_id = dblink_linked_recid
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and szm_object_type = mrkr_type
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and fdb_db_name = 'UniProtKB'
order by 1;
\copy (select * from uniprot) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/uniprot.txt' with delimiter as '	' null as '';
drop view uniprot;

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/interpro.txt' with delimiter as '	' null as '';"
create view interpro as
select mrkr_zdb_id, szm_term_ont_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db, so_zfin_mapping
 where mrkr_zdb_id = dblink_linked_recid
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdb_db_name = 'InterPro'
order by 1;
\copy (select * from interpro) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/interpro.txt' with delimiter as '	' null as '';
drop view interpro;

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/pfam.txt' with delimiter as '	' null as '';"
create view pfam as
select mrkr_zdb_id, szm_term_ont_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db, so_zfin_mapping
 where mrkr_zdb_id = dblink_linked_recid
  and fdbcont_fdb_db_id = fdb_db_pk_id
  and szm_object_type = mrkr_type
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdb_db_name = 'Pfam'
order by 1;
\copy (select * from pfam) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/pfam.txt' with delimiter as '	' null as '';
drop view pfam;

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genpept.txt' with delimiter as '	' null as '';"
create view genpept as
select mrkr_zdb_id, szm_term_ont_id, mrkr_abbrev,dblink_acc_num from marker, db_link, foreign_db_contains, foreign_db, so_zfin_mapping
 where mrkr_zdb_id = dblink_linked_recid
   and fdbcont_fdb_db_id = fdb_db_pk_id
  and szm_object_type = mrkr_type
   and fdbcont_zdb_id = dblink_fdbcont_zdb_id
   and fdb_db_name = 'GenPept'
 order by 1;
\copy (select * from genpept) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genpept.txt' with delimiter as '	' null as '';
drop view genpept;

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/vega.txt' with delimiter as '	' null as '';"
create view vega as
select mrkr_zdb_id, szm_term_ont_id,mrkr_abbrev,dblink_acc_num
 from marker, db_link, foreign_db_contains, foreign_db, so_zfin_mapping
 where mrkr_zdb_id = dblink_linked_recid
and szm_object_type = mrkr_type
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdbcont_zdb_id = dblink_fdbcont_zdb_id
   and fdb_db_name in ('VEGA','unreleasedRNA')
 order by 1;
\copy (select * from vega) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/vega.txt' with delimiter as '	' null as '';
drop view vega;

-- vega_transcript.txt is only used by tomc and Sanger.
-- please check before changing thanks.

--! echo "'<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/vega_transcript.txt' with delimiter as '	' null as '';"
create view vega_transcript as
select distinct mrkr_zdb_id, szm_term_ont_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, marker_relationship, so_zfin_mapping
 where mrel_type = 'gene produces transcript'
   and substring(dblink_acc_num from 1 for 8) = 'OTTDART0'
   and szm_object_type = mrkr_type
   and mrel_mrkr_2_zdb_id = dblink_linked_recid
   and mrel_mrkr_1_zdb_id = mrkr_zdb_id
   and not (
   	substring(dblink_linked_recid from 1 for 12) = 'ZDB-TSCRIPT-'
   	and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-37'
   ) order by 1
;
\copy (select * from vega_transcript) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/vega_transcript.txt' with delimiter as '	' null as '';
drop view vega_transcript;

-- the changing assembly version number in db_name
-- is apt to come back to bite us so I am opting for the zdb_id
--! echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/ensembl_1_to_1.txt' with delimiter as '	' null as '';'
create view ensembl_1_to_1 as
select mrkr_zdb_id, szm_term_ont_id, mrkr_abbrev,dblink_acc_num
 from marker, db_link, so_zfin_mapping
 where mrkr_zdb_id = dblink_linked_recid
   and szm_object_type = mrkr_type
   and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
 order by 1;
\copy (select * from ensembl_1_to_1) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/ensembl_1_to_1.txt' with delimiter as '	' null as ''; 
drop view ensembl_1_to_1;

--! echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/all_rna_accessions.txt' with delimiter as '	' null as '';'
create view all_rna_accessions as
select distinct gene.mrkr_zdb_id gene_zdb, szm_term_ont_id, gene.mrkr_abbrev gene_sym,dblink_acc_num accession
 from db_link, marker gene, marker_type_group_member,foreign_db_contains, foreign_db, foreign_db_data_type, so_zfin_mapping
 where dblink_linked_recid=gene.mrkr_zdb_id
   and gene.mrkr_type=mtgrpmem_mrkr_type
   and mtgrpmem_mrkr_type_group='GENEDOM'
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdbdt_id = fdbdt_pk_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and szm_object_type = mrkr_type
   and fdbdt_data_type='RNA'
   -- and fdb_db_name not like '%Vega%' and fdb_db_name not like '%VEGA%'
   -- note: the below would have the same effect as above
   --       but since neither catch anything go with the faster/cheaper
   and substring(dblink_acc_num from 1 for 6) != 'OTTDAR'
union
select distinct gene.mrkr_zdb_id gene_zdb, szm_term_ont_id,
       gene.mrkr_abbrev gene_sym,
       dblink_acc_num accession
 from marker gene,marker_type_group_member, marker est, db_link, marker_relationship, foreign_db_contains,
 	foreign_db, foreign_db_data_type, so_zfin_mapping
 where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id
   and est.mrkr_zdb_id  = mrel_mrkr_2_zdb_id
   and mrel_type = 'gene encodes small segment'
   and est.mrkr_zdb_id = dblink_linked_recid
   and szm_object_type = gene.mrkr_type
   and est.mrkr_type in ('EST','CDNA')
    and gene.mrkr_type=mtgrpmem_mrkr_type
   and mtgrpmem_mrkr_type_group='GENEDOM'
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdbcont_fdbdt_id = fdbdt_pk_id
   and fdbcont_fdb_db_id = fdb_db_pk_id
   and fdbdt_data_type='RNA'
   -- and fdb_db_name not like '%Vega%' and fdb_db_name not like '%VEGA%'
   -- note: the below would have the same effect as above
   --       but since neither catch anything go with the faster/cheaper
   and substring(dblink_acc_num from 1 for 6) != 'OTTDAR'
;
\copy (select * from all_rna_accessions) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/all_rna_accessions.txt' with delimiter as '	' null as '';
drop view all_rna_accessions;

-- Generate genotype_feature file

create temp table tmp_geno_data (
  genotype_id text,
  geno_display_name text,
  geno_handle text,
  feature_name text,
  feature_abbrev text,
  feature_type text,
  feature_type_display text,
  gene_abbrev text,
  gene_id text,
  feature_zdb_id text,
  zygocity text,
  construct_zdb_id text,
  construct_name text
);


insert into tmp_geno_data (
	genotype_id, geno_display_name, geno_handle, feature_name, feature_abbrev,
	feature_type, feature_type_display,feature_zdb_id, zygocity, gene_id, gene_abbrev
)
select
	genofeat_geno_zdb_id,
	geno_display_name,
	geno_handle,
	feature_name,
	feature_abbrev,
	lower(feature_type),
	ftrtype_type_display,
	feature_zdb_id,
	zyg_name,
	mrkr_zdb_id,
	mrkr_abbrev
 from genotype_feature, feature, genotype, feature_type, zygocity, marker, feature_marker_relationship
 where genofeat_feature_zdb_id = feature_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
   and feature_type = ftrtype_name
   and genofeat_zygocity = zyg_zdb_id
   and fmrel_ftr_zdb_id = feature_zdb_id
   and fmrel_mrkr_zdb_id = mrkr_zdb_id
   and fmrel_type = 'is allele of';

insert into tmp_geno_data (
	genotype_id, geno_display_name, geno_handle, feature_name, feature_abbrev,
	feature_type, feature_type_display,feature_zdb_id, zygocity
)
select
	genofeat_geno_zdb_id,
	geno_display_name,
	geno_handle,
	feature_name,
	feature_abbrev,
	lower(feature_type),
	ftrtype_type_display,
	feature_zdb_id,
	zyg_name
 from genotype_feature, feature, genotype, feature_type, zygocity
 where genofeat_feature_zdb_id = feature_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
   and feature_type = ftrtype_name
   and genofeat_zygocity = zyg_zdb_id
   and not exists ( select 'x' from marker, feature_marker_relationship
   where fmrel_ftr_zdb_id = feature_zdb_id
   and fmrel_mrkr_zdb_id = mrkr_zdb_id
   and fmrel_type = 'is allele of'
   );

create temp table tmp_dups (
  fmrel_ftr_zdb_id text
);

insert into tmp_dups (fmrel_ftr_zdb_id)
select fmrel_ftr_zdb_id
 from feature_marker_relationship
 where fmrel_type like 'contains%'
 group by fmrel_ftr_zdb_id having count(*) > 1;

delete from tmp_geno_data
 where feature_zdb_id in (select fmrel_ftr_zdb_id from tmp_dups) ;

update tmp_Geno_data
 set construct_zdb_id = (Select fmrel_mrkr_Zdb_id from feature_marker_relationship
     		      		where fmrel_ftr_zdb_id = feature_Zdb_id
				and fmrel_type like 'contains%')
 where feature_zdb_id not in (select fmrel_ftr_zdb_id from tmp_dups) ;

update tmp_Geno_data
 set construct_name = (Select mrkr_name from feature_marker_relationship, marker
     		      		where fmrel_ftr_zdb_id = feature_Zdb_id
				and fmrel_type like 'contains%'
				and mrkr_zdb_id = fmrel_mrkr_zdb_id)
 where feature_zdb_id not in (select fmrel_ftr_zdb_id from tmp_dups) ;

create temp table tmp_extras (
        genofeat_geno_zdb_id text,
  	geno_display_name text,
  	geno_handle text,
  	feature_name text,
  	feature_abbrev text,
  	feature_type text,
  	ftrtype_type_display text,
  	feature_zdb_id text,
  	zyg_name text,
	mrkr_zdb_id text,
	mrkr_abbrev text,
	construct_zdb_id text,
	construct_name text
);
insert into tmp_extras (
        genofeat_geno_zdb_id,
  	geno_display_name,
  	geno_handle,
  	feature_name,
  	feature_abbrev,
  	feature_type,
  	ftrtype_type_display,
  	feature_zdb_id,
  	zyg_name,
	mrkr_zdb_id,
	mrkr_abbrev,
	construct_zdb_id,
	construct_name
)
select
	genofeat_geno_zdb_id,
	geno_display_name,
	geno_handle,
	feature_name,
	feature_abbrev,
	lower(feature_type),
	ftrtype_type_display,
	feature_zdb_id,
	zyg_name,
	mrkr_zdb_id,
	mrkr_abbrev, '' as construct_Zdb_id,'' as construct_name
 from genotype_feature, feature, genotype, feature_type, zygocity, marker, feature_marker_relationship
 where genofeat_feature_zdb_id = feature_zdb_id
  and exists (Select 'x' from tmp_dups where feature_zdb_id = fmrel_ftr_zdb_id)
   and geno_zdb_id = genofeat_geno_zdb_id
   and feature_type = ftrtype_name
   and genofeat_zygocity = zyg_zdb_id
   and fmrel_ftr_zdb_id = feature_zdb_id
   and fmrel_mrkr_zdb_id = mrkr_zdb_id
 and fmrel_type not like 'contains%'
union
select
	genofeat_geno_zdb_id,
	geno_display_name,
	geno_handle,
	feature_name,
	feature_abbrev,
	lower(feature_type),
	ftrtype_type_display,
	feature_zdb_id,
	zyg_name,
	'' as gene_id,
	'' as gene_name, mrkr_zdb_id,mrkr_name
 from genotype_feature, feature, genotype, feature_type, zygocity, marker, feature_marker_relationship
 where genofeat_feature_zdb_id = feature_zdb_id
   and exists (Select 'x' from tmp_dups where feature_zdb_id = fmrel_ftr_zdb_id)
   and geno_zdb_id = genofeat_geno_zdb_id
   and feature_type = ftrtype_name
   and genofeat_zygocity = zyg_zdb_id
   and fmrel_ftr_zdb_id = feature_zdb_id
   and fmrel_mrkr_zdb_id = mrkr_zdb_id
 and fmrel_type like 'contains%'
;

insert into tmp_geno_data(
	genotype_id, geno_display_name, geno_handle, feature_name, feature_abbrev,
	feature_type, feature_type_display,feature_zdb_id, zygocity, gene_id, gene_abbrev, construct_zdb_id, construct_name
) select * from tmp_extras;


\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genotype_features.txt' with delimiter as '	' null as '';'
create view genotype_features as
select distinct
	genotype_id,
	geno_display_name,
	geno_handle,
	feature_zdb_id,
	feature_name,
	feature_abbrev,
	feature_type,
	feature_type_display,
	gene_abbrev,
	gene_id,
	zygocity,
	construct_name,
	construct_zdb_id
 from tmp_geno_data
 order by genotype_id, geno_display_name
;
\copy (select * from genotype_features) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genotype_features.txt' with delimiter as '	' null as '';
drop view genotype_features;
drop table tmp_dups;
drop table tmp_extras;
drop table tmp_geno_data;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/tgInsertions.txt' with delimiter as '	' null as '';'
create view tgInsertions as
select distinct feature_zdb_id, feature_abbrev, feature_name, a.szm_term_ont_id, fmrel_mrkr_zdb_id, mrkr_name, b.szm_term_ont_id as id2
                     from feature, feature_marker_Relationship, marker, so_zfin_mapping a, so_zfin_mapping b
                    where fmrel_ftr_zdb_id = feature_zdb_id
		    and feature_type = 'TRANSGENIC_INSERTION'
                    and fmrel_mrkr_zdb_id = mrkr_zdb_id
		    and get_obj_type(mrkr_zdb_id) = b.szm_object_type
		    and feature_type = a.szm_object_type;
\copy (select * from tgInsertions) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/tgInsertions.txt' with delimiter as '	' null as '';
drop view tgInsertions;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/constructComponents.txt' with delimiter as '	' null as '';'
create view constructComponents as
select distinct a.mrkr_zdb_id,
                a.mrkr_name,
                a.mrkr_type,
                b.mrkr_zdb_id as id2,
                b.mrkr_name as name2,
                b.mrkr_type as type2,
		mrel_type,
                c.szm_term_ont_id,
                d.szm_term_ont_id as ont2
         from marker a, marker b, marker_Relationship, so_zfin_mapping c, so_zfin_mapping d
 	 where a.mrkr_zdb_id = mrel_mrkr_1_zdb_id
	 and b.mrkr_zdb_id = mrel_mrkr_2_zdb_id
	 and get_obj_type(a.mrkr_zdb_id) = c.szm_object_type
	 and get_obj_type(b.mrkr_zdb_id) = d.szm_object_type
	 and a.mrkr_type in ('TGCONSTRCT','GTCONSTRCT','ETCONSTRCT','PTCONSTRCT')
         order by a.mrkr_zdb_id ;
\copy (select * from constructComponents) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/constructComponents.txt' with delimiter as '	' null as '';
drop view constructComponents;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genotype_features_missing_markers.txt' with delimiter as '	' null as '';'
create view genotype_features_missing_markers as
select distinct  geno_zdb_id, geno_display_name, geno_handle, mrkr_abbrev, mrkr_zdb_id
 from feature_marker_relationship,marker_type_group_member, feature, genotype, genotype_feature, marker
 where fmrel_ftr_zdb_id=feature_zdb_id
   and fmrel_mrkr_zdb_id=mrkr_zdb_id
   and fmrel_type in ('markers missing', 'markers moved')
   and mrkr_type=mtgrpmem_mrkr_type
   and mtgrpmem_mrkr_type_group='GENEDOM'
   and feature_zdb_id = genofeat_feature_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
;
\copy (select * from genotype_features_missing_markers) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genotype_features_missing_markers.txt' with delimiter as '	' null as '';
drop view genotype_features_missing_markers;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genotype_backgrounds.txt' with delimiter as '	' null as '';'
create view genotype_backgrounds as
select distinct a.geno_zdb_id, a.geno_display_name, genoback_background_zdb_id,b.geno_display_name as name2
 from genotype a, genotype_background, genotype b
 where a.geno_Zdb_id = genoback_geno_Zdb_id
 and b.geno_zdb_id=genoback_background_zdb_id
;
\copy (select * from genotype_backgrounds) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/genotype_backgrounds.txt' with delimiter as '	' null as '';
drop view genotype_backgrounds;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/wildtypes_fish.tx''
create view wildtypes_fish as
select distinct fish_zdb_id, fish_full_name, fish_handle, geno_zdb_id
 from genotype, fish
 where geno_is_wildtype = 't'
  and fish_genotype_Zdb_id = geno_zdb_id
 and not exists (Select 'x' from fish_str where fishstr_fish_zdb_id = fish_zdb_id)
;
\copy (select * from wildtypes_fish) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/wildtypes_fish.txt' with delimiter as '	' null as '';
drop view wildtypes_fish;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/fish_components_fish.tx''
create view fish_components_fish as
select fc_fish_zdb_id, fc_fish_name, fc_gene_zdb_id, a.mrkr_abbrev, fc_affector_zdb_id, b.mrkr_abbrev as abbr2, fc_construct_zdb_id, c.mrkr_abbrev as abbr3, genoback_background_zdb_id, d.geno_handle, fc_genotype_Zdb_id, e.geno_display_name
   from fish_components 
   join genotype e
     on fc_genotype_zdb_id =e.geno_zdb_id
   full outer join genotype_background
     on fc_genotype_zdb_id = genoback_geno_zdb_id
   full outer join genotype d 
     on genoback_background_zdb_id = d.geno_Zdb_id
   full outer join marker a
     on a.mrkr_Zdb_id = fc_gene_zdb_id
   full outer join marker b 
     on b.mrkr_zdb_id = fc_affector_zdb_id
   full outer join marker c
     on c.mrkr_Zdb_id = fc_construct_zdb_id
   where fc_affector_zdb_id not like 'ZDB-ALT%'
union
select fc_fish_zdb_id, fc_fish_name, fc_gene_zdb_id, a.mrkr_abbrev, fc_affector_zdb_id, b.feature_abbrev as abbr2, fc_construct_zdb_id, c.mrkr_abbrev as abbr3, genoback_background_zdb_id, d.geno_handle, fc_genotype_Zdb_id, e.geno_display_name
   from fish_components
   join genotype e
     on fc_genotype_zdb_id =e.geno_zdb_id
   full outer join genotype_background 
     on fc_genotype_zdb_id = genoback_geno_zdb_id
   full outer join genotype d
     on genoback_background_zdb_id = d.geno_Zdb_id
   full outer join marker a
     on a.mrkr_Zdb_id = fc_gene_zdb_id
   full outer join feature b
     on b.feature_zdb_id = fc_affector_zdb_id
   full outer join marker c
     on c.mrkr_Zdb_id = fc_construct_zdb_id
   where fc_affector_zdb_id like 'ZDB-ALT%'
;
\copy (select * from fish_components_fish) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/fish_components_fish.txt' with delimiter as '	' null as '';
drop view fish_components_fish;

-- generate a file with zdb history data
\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/zdb_history.txt' with delimiter as '	' null as '';'
create view zdb_history as
select zrepld_old_zdb_id, zrepld_new_zdb_id
from zdb_replaced_data
order by zrepld_old_zdb_id, zrepld_new_zdb_id
;
\copy (select * from zdb_history) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/zdb_history.txt' with delimiter as '	' null as '';
drop view zdb_history;

-- indirect sequence links for genes

create temp table tmp_veg (gene_zdb text,
                           szm_term_ont_id text,
                           gene_sym text,
                           genbank_acc text);

insert into tmp_veg(gene_zdb, szm_term_ont_id, gene_sym, genbank_acc)
select distinct gene.mrkr_zdb_id, szm_term_ont_id, gene.mrkr_abbrev, dblink_acc_num
from marker gene, marker_type_group_member,marker est, db_link, marker_relationship,foreign_db, foreign_db_contains, so_zfin_mapping
 where gene.mrkr_zdb_id = mrel_mrkr_1_zdb_id
   and   est.mrkr_zdb_id  = mrel_mrkr_2_zdb_id
   and gene.mrkr_type = szm_object_type
   and  mrel_type = 'gene encodes small segment'
   and est.mrkr_zdb_id = dblink_linked_recid
   and est.mrkr_type  in ('EST','CDNA')
   and gene.mrkr_type = mtgrpmem_mrkr_type
   and mtgrpmem_mrkr_type_group='GENEDOM'
   and dblink_fdbcont_zdb_id = fdbcont_zdb_id
   and fdb_db_name = 'GenBank'
   and fdbcont_fdb_db_id = fdb_db_pk_id;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/gene_seq.txt' with delimiter as '	' null as '';'
\copy (select * from tmp_veg order by 1,3) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/gene_seq.txt' with delimiter as '	' null as '';
drop table tmp_veg;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/stage_ontology.txt' with delimiter as '	' null as '';'
create view stage_ontology as
select stg_zdb_id, stg_obo_id, stg_name, stg_hours_start, stg_hours_end
  from stage
  order by stg_hours_start, stg_hours_end desc
;
\copy (select * from stage_ontology) to  '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/stage_ontology.txt' with delimiter as '	' null as '';
drop view stage_ontology;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/anatomy_item.txt' with delimiter as '	' null as '';'
create view anatomy_item as
select term_ont_id, term_name, ts_start_stg_zdb_id, ts_end_stg_zdb_id
 from term, term_stage
 where term_zdb_id = ts_term_zdb_id
   and term_ontology = 'zebrafish_anatomy'
 order by term_name
 ;
\copy (select * from anatomy_item) to  '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/anatomy_item.txt' with delimiter as '	' null as '';
drop view anatomy_item;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/anatomy_relationship.txt' with delimiter as '	' null as '';'
create view anatomy_relationship as
select term1.term_ont_id, term2.term_ont_id as id2, termrel_type
 from term_relationship, term as term1, term as term2
 where term1.term_ontology = 'zebrafish_anatomy'
   and term2.term_ontology = 'zebrafish_anatomy'
   and term1.term_zdb_id = termrel_term_1_zdb_id
   and term2.term_zdb_id = termrel_term_2_zdb_id
;
\copy (select * from anatomy_relationship) to  '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/anatomy_relationship.txt' with delimiter as '	' null as '';
drop view anatomy_relationship;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpat_stage_anatomy.txt' with delimiter as '	' null as '';'
create view xpat_stage_anatomy as
select xpatres_zdb_id,
       xpatres_xpatex_zdb_id,
       xpatres_start_stg_zdb_id,
       xpatres_end_stg_zdb_id,
       superterm.term_ont_id,
       subterm.term_ont_id as id2,
       xpatres_expression_found
 from expression_result join term superterm
                          on superterm.term_zdb_id = xpatres_superterm_zdb_id
                        join expression_Experiment
                          on xpatex_zdb_id = xpatres_xpatex_zdb_id
                        full outer join term subterm 
                          on subterm.term_zdb_id = xpatres_subterm_zdb_id
                        full outer join clone
                          on clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
 where not exists(select 'x' from expression_experiment, marker
                   where xpatex_zdb_id = xpatres_xpatex_zdb_id
                     and xpatex_gene_zdb_id is not null
                     and xpatex_gene_zdb_id = mrkr_zdb_id
                     and mrkr_abbrev like 'WITHDRAWN%')
 order by xpatres_xpatex_zdb_id
;
\copy (select * from xpat_stage_anatomy) to  '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/xpat_stage_anatomy.txt' with delimiter as '	' null as '';
drop view xpat_stage_anatomy;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/anatomy_synonyms.txt' with delimiter as '	' null as '';'
create view anatomy_synonyms as
select term_ont_id, term_name, dalias_alias
 from data_alias, term, alias_group
 where dalias_data_zdb_id = term_zdb_id
   and substring(dalias_data_zdb_id from 1 for 8) = 'ZDB-TERM'
   and substring(dalias_alias from 1 for 4) != 'ZFA:'
   and dalias_group_id = aliasgrp_pk_id
   and term_ontology = 'zebrafish_anatomy'
   and aliasgrp_name not in ('plural','secondary id')
 order by term_name
;
\copy (select * from anatomy_synonyms) to  '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/anatomy_synonyms.txt' with delimiter as '	' null as '';
drop view anatomy_synonyms;

-- Image data
\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/ImageFigures.txt' with delimiter as '	' null as '';'
create view ImageFigures as
select img_zdb_id, img_fig_zdb_id, img_preparation
 from image
 where img_fig_zdb_id is not null
 order by img_zdb_id;
\copy (select * from ImageFigures) to  '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/ImageFigures.txt' with delimiter as '	' null as '';
drop view ImageFigures;

-- Transcript data
-- Get clones and genes if available but still report if not (a small subset)
\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/transcripts.txt' with delimiter as '	' null as '';'
create view transcripts as
select t.tscript_mrkr_zdb_id,szm_term_ont_id,m.mrkr_name,'no related gene','no related clone' as cloneRelated ,tt.tscriptt_type,ts.tscripts_status
 from transcript t
 join marker m on m.mrkr_zdb_id=t.tscript_mrkr_zdb_id
 join so_zfin_mapping on szm_object_type = m.mrkr_type
 join transcript_type tt on tt.tscriptt_pk_id=t.tscript_type_id
 left outer join transcript_status ts on ts.tscripts_pk_id=t.tscript_status_id
 where not exists (
	select 'x'
	 from marker gene , marker_relationship mr
	 where gene.mrkr_zdb_id=mr.mrel_mrkr_1_zdb_id
	   and mr.mrel_mrkr_2_zdb_id=m.mrkr_zdb_id
	   and mr.mrel_type in ('gene produces transcript','clone contains transcript')
)
union
select t.tscript_mrkr_zdb_id,szm_term_ont_id,m.mrkr_name,gene.mrkr_zdb_id,c.mrkr_zdb_id,tt.tscriptt_type,ts.tscripts_status
 from transcript t
 join marker m on m.mrkr_zdb_id=t.tscript_mrkr_zdb_id
 join transcript_type tt on tt.tscriptt_pk_id=t.tscript_type_id
 left outer join transcript_status ts on ts.tscripts_pk_id=t.tscript_status_id
 join so_zfin_mapping on szm_object_type = m.mrkr_type
 join marker_relationship gener on gener.mrel_mrkr_2_zdb_id=m.mrkr_zdb_id
 join marker_relationship cloner on cloner.mrel_mrkr_2_zdb_id=m.mrkr_zdb_id
 join marker gene on gene.mrkr_zdb_id=gener.mrel_mrkr_1_zdb_id
 join marker c on c.mrkr_zdb_id=cloner.mrel_mrkr_1_zdb_id
 where gener.mrel_type='gene produces transcript'
   and cloner.mrel_type='clone contains transcript'
;
\copy (select * from transcripts) to  '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/transcripts.txt' with delimiter as '	' null as '';
drop view transcripts;

create temp table tmp_pubs (
        mrkr_zdb_id text,
  	mrkr_abbrev text,
  	mrkr_type text,
  	source_id text
);
insert into tmp_pubs(mrkr_zdb_id, mrkr_abbrev, mrkr_type, source_id)
  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra, marker m
  where m.mrkr_zdb_id = ra.recattrib_data_zdb_id

  UNION 

  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  INNER JOIN marker_relationship mr ON ra.recattrib_data_zdb_id = mr.mrel_zdb_id
  INNER JOIN marker m ON m.mrkr_zdb_id = mr.mrel_mrkr_2_zdb_id

  UNION -- marker relationship 1_2

  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  JOIN marker_relationship mr ON ra.recattrib_data_zdb_id = mr.mrel_zdb_id
  JOIN marker m ON m.mrkr_zdb_id = mr.mrel_mrkr_1_zdb_id

  UNION -- morhpolino marker type ? necessary

  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  JOIN marker_relationship mr ON ra.recattrib_data_zdb_id = mr.mrel_mrkr_1_zdb_id
  JOIN marker mrph ON mr.mrel_mrkr_1_zdb_id = mrph.mrkr_zdb_id
  JOIN marker m ON m.mrkr_zdb_id = mr.mrel_mrkr_2_zdb_id
  where mrph.mrkr_type = 'MRPHLNO'

  UNION -- data alias

  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  JOIN data_alias da ON da.dalias_zdb_id = ra.recattrib_data_zdb_id
  JOIN marker m ON m.mrkr_zdb_id = da.dalias_data_zdb_id

  UNION -- db link

  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  JOIN db_link dbl ON dbl.dblink_zdb_id = ra.recattrib_data_zdb_id
  JOIN marker m ON m.mrkr_zdb_id = dbl.dblink_linked_recid

  UNION -- db link, marker_relationship

  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  JOIN db_link dbl ON dbl.dblink_zdb_id  = ra.recattrib_data_zdb_id
  JOIN marker_relationship mr ON dbl.dblink_linked_recid = mr.mrel_mrkr_2_zdb_id
  JOIN marker m ON m.mrkr_zdb_id = mr.mrel_mrkr_1_zdb_id
  WHERE mr.mrel_type = 'gene encodes small segment'

  UNION -- ortho

  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  JOIN ortholog ev ON ev.ortho_zdb_id = ra.recattrib_data_zdb_id
  JOIN marker m ON m.mrkr_zdb_id = ev.ortho_zebrafish_gene_zdb_id

  UNION -- marker_go_term_Evidence

  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  JOIN marker_go_term_evidence ev ON ev.mrkrgoev_zdb_id = ra.recattrib_data_zdb_id
  JOIN marker m ON m.mrkr_zdb_id = ev.mrkrgoev_mrkr_zdb_id

  UNION -- feature_marker_realationship

  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  JOIN feature_marker_relationship fmr ON fmr.fmrel_ftr_zdb_id = ra.recattrib_data_zdb_id
  JOIN marker m ON m.mrkr_zdb_id = fmr.fmrel_mrkr_zdb_id

  UNION -- feature_marker_realationship, genotype_feature
  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  JOIN genotype_feature gf ON gf.genofeat_geno_zdb_id  = ra.recattrib_data_zdb_id
  JOIN feature_marker_relationship fmr ON fmr.fmrel_ftr_zdb_id  = gf.genofeat_feature_zdb_id
  JOIN marker m ON m.mrkr_zdb_id = fmr.fmrel_mrkr_zdb_id

  UNION -- genotype_feature

  SELECT m.mrkr_zdb_id,
         m.mrkr_abbrev,
         m.mrkr_type,
         ra.recattrib_source_zdb_id
  FROM record_attribution ra
  JOIN genotype_feature gf ON gf.genofeat_geno_zdb_id = ra.recattrib_data_zdb_id
  JOIN marker m ON m.mrkr_zdb_id = gf.genofeat_feature_zdb_id

  UNION -- expression_experiment
  SELECT mrkr_zdb_id,
         mrkr_abbrev,
         mrkr_type,
         ex.xpatex_source_zdb_id
  FROM expression_experiment ex,  marker,marker_type_group_member
 where mrkr_zdb_id = ex.xpatex_gene_zdb_id
AND mrkr_type =mtgrpmem_mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM'
--ORDER BY mrkr_abbrev_order
 and not exists (Select 'x' from clone
     	 		where clone_mrkr_zdb_id = ex.xpatex_probe_feature_zdb_id
 			 and clone_problem_type = 'Chimeric')
;

-- publication - gene/genep association file
\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/gene_publication.txt' with delimiter as '	' null as '';'
create view gene_publication as
SELECT distinct mrkr_abbrev,
       mrkr_zdb_id,
       source_id,
       CASE pub.jtype
           WHEN 'Unpublished' THEN 'Data Submission'
           ELSE pub.jtype
       END,
       pub.accession_no
FROM publication pub, tmp_pubs,marker_type_group_member
 where source_id = zdb_id
 and mrkr_type=mtgrpmem_mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM_AND_NTR';
\copy (select * from gene_publication) to  '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/gene_publication.txt' with delimiter as '	' null as '';
drop view gene_publication;

create temp table tmp_wtxpat (
        mrkr_zdb_id text,
  	mrkr_abbrev text,
  	fish_full_name text,
  	term_ont_id text,
  	term_name text,
  	subontid text,
  	subname text,
  	startSt text,
  	endSt text,
  	xpatex_assay_name text,
  	xpatex_source_zdb_id text,
  	probe_id text,
  	antibody_id text,
  	fish_zdb_id text
);
insert into tmp_wtxpat (
        mrkr_zdb_id,
  	mrkr_abbrev,
  	fish_full_name,
  	term_ont_id,
  	term_name,
  	subontid,
  	subname,
  	startSt,
  	endSt,
  	xpatex_assay_name,
  	xpatex_source_zdb_id,
  	probe_id,
  	antibody_id,
  	fish_zdb_id
)
select mrkr_zdb_id, mrkr_abbrev, fish_full_name, super.term_ont_id, super.term_name,
       '' as subontid,
       '' as subname, startStage.stg_name as startSt, endStage.stg_name as endSt, xpatex_assay_name,
        xpatex_source_zdb_id,
        case when xpatex_probe_feature_zdb_id = '' then ' ' else xpatex_probe_feature_zdb_id end as probe_id,
        case when xpatex_atb_zdb_id = '' then ' ' else xpatex_atb_zdb_id end as antibody_id, fish_zdb_id
 from marker, expression_experiment, fish_experiment, fish, experiment, expression_result, stage startStage, stage endStage,
 term super, genotype
 where geno_is_wildtype = 't'
   and exp_zdb_id in ('ZDB-EXP-041102-1','ZDB-EXP-070511-5')
   and xpatres_expression_found = 't'
  and not exists (Select 'x' from clone
  where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
  and clone_problem_type = 'Chimeric')
   and mrkr_zdb_id = xpatex_gene_zdb_id
   and xpatex_genox_zdb_id = genox_zdb_id
   and xpatres_superterm_zdb_id = super.term_zdb_id
   and fish_zdb_id = genox_fish_zdb_id
--   and fish_is_wildtype = 't'
   and not exists (Select 'x' from fish_Str where fish_Zdb_id = fishstr_Fish_zdb_id)
   and xpatres_xpatex_zdb_id = xpatex_zdb_id
   and xpatres_start_stg_zdb_id = startStage.stg_zdb_id
   and xpatres_end_stg_zdb_id = endStage.stg_zdb_id
   and fish_genotype_zdb_id = geno_zdb_id
and xpatres_subterm_zdb_id is null
 group by mrkr_zdb_id, mrkr_abbrev, fish_full_name, super.term_ont_id, super.term_name,
        subontid, subname, startStage.stg_name, endStage.stg_name, xpatex_assay_name,
        xpatex_source_zdb_id,  probe_id,xpatex_atb_zdb_id, fish_Zdb_id
union
select mrkr_zdb_id, mrkr_abbrev, fish_full_name, super.term_ont_id, super.term_name,
       sub.term_ont_id as subontid,
       sub.term_name as subname, startStage.stg_name as startSt, endStage.stg_name as endSt, xpatex_assay_name,
        xpatex_source_zdb_id,
        case when xpatex_probe_feature_zdb_id = '' then ' ' else xpatex_probe_feature_zdb_id end as probe_id,
        case when xpatex_atb_zdb_id = '' then ' ' else xpatex_atb_zdb_id end as antibody_id, fish_zdb_id
 from marker, expression_experiment, fish_experiment, fish, experiment, expression_result, stage startStage, stage endStage,
 term super, genotype, term sub
 where geno_is_wildtype = 't'
   and exp_zdb_id in ('ZDB-EXP-041102-1','ZDB-EXP-070511-5')
   and xpatres_expression_found = 't'
  and not exists (Select 'x' from clone
      	  	 	 where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
  and clone_problem_type = 'Chimeric')
   and mrkr_zdb_id = xpatex_gene_zdb_id
   and xpatex_genox_zdb_id = genox_zdb_id
   and xpatres_superterm_zdb_id = super.term_zdb_id
   and fish_zdb_id = genox_fish_zdb_id
--   and fish_is_wildtype = 't'
   and not exists (Select 'x' from fish_Str where fish_Zdb_id = fishstr_Fish_zdb_id)
   and xpatres_xpatex_zdb_id = xpatex_zdb_id
   and xpatres_start_stg_zdb_id = startStage.stg_zdb_id
   and xpatres_end_stg_zdb_id = endStage.stg_zdb_id
   and fish_genotype_zdb_id = geno_zdb_id
   and sub.term_zdb_id = xpatres_subterm_zdb_id
and xpatres_subterm_zdb_id is not null
 group by mrkr_zdb_id, mrkr_abbrev, fish_full_name, super.term_ont_id, super.term_name,
        subontid, subname, startStage.stg_name, endStage.stg_name, xpatex_assay_name,
        xpatex_source_zdb_id,  probe_id,xpatex_atb_zdb_id, fish_Zdb_id
;

delete from tmp_wtxpat
 where probe_id in (select clone_mrkr_zdb_id from clone where clone_problem_type = 'Chimeric');


-- create full expression file for WT fish: standard condition, expression shown and
-- only wildtype fish
\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/wildtype-expression_fish2.txt' with delimiter as '	' null as '';'
\copy (select * from tmp_wtxpat) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/wildtype-expression_fish2.txt' with delimiter as '	' null as '';

--case 8490 and case, 8886. Report of all publications that use an sa allele
--not for public consumption
--only for Sanger, will be picked up by sanger folks.

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/saAlleles2.txt' with delimiter as '	' null as '';'
create view saAlleles as
select distinct recattrib_source_zdb_id, accession_no, pub_mini_ref ||' '||jrnl_name ||' '|| ' ' || pub_volume ||' '|| pub_pages, feature_abbrev
from feature, record_attribution, publication, journal
where recattrib_data_zdb_id=feature_zdb_id
and feature_abbrev like 'sa%'
and zdb_id=recattrib_source_zdb_id
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
order by feature_abbrev;
\copy (select * from saAlleles) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/saAlleles2.txt' with delimiter as '	' null as '';
drop view saAlleles;

--case 9235
\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/fhAlleles.txt' with delimiter as '	' null as '';'
create view fhAlleles as
select distinct recattrib_source_zdb_id, accession_no, pub_mini_ref ||' '||jrnl_name ||' '|| ' ' || pub_volume ||' '|| pub_pages, feature_abbrev
from feature, record_attribution, publication, journal
where recattrib_data_zdb_id=feature_zdb_id
and feature_abbrev like 'fh%'
and zdb_id=recattrib_source_zdb_id
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
order by feature_abbrev;
\copy (select * from fhAlleles) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/fhAlleles.txt' with delimiter as '	' null as '';
drop view fhAlleles;

-- case 13856
\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/huAlleles2.txt' with delimiter as '	' null as '';'
create view huAlleles as
select distinct recattrib_source_zdb_id, accession_no, pub_mini_ref ||' '||jrnl_name ||' '|| ' ' || pub_volume ||' '|| pub_pages, feature_abbrev
from feature, record_attribution, publication, journal
where recattrib_data_zdb_id=feature_zdb_id
and feature_abbrev like 'hu%'
and zdb_id=recattrib_source_zdb_id
and jtype='Journal'
and pub_jrnl_zdb_id=jrnl_zdb_id
order by feature_abbrev;
\copy (select * from huAlleles) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/huAlleles2.txt' with delimiter as '	' null as '';
drop view huAlleles;

--case 4402  Weekly download file available via the web.
--Fields: OMIM, ZFIN-GENE-ID, ZFIN-GENO-ID, ZIRC-ALT-ID
--All lines available from ZIRC.

-- download file Case 4200 as reuqested by uniprot

create temp table tmp_gene_pubcount (geneid text, pubcount int)
;

insert into tmp_gene_pubcount
select recattrib_data_zdb_id geneid, count(recattrib_source_zdb_id) pubcount
 from record_attribution
 where substring(recattrib_data_zdb_id from 1 for 8) in ('ZDB-GENE', 'ZDB-LNCR','ZDB-LINC','ZDB-TRNA','ZDB-RRNA','ZDB-MIRN','ZDB-SNOR','ZDB-NCRN','ZDB-PIRN')
 group by recattrib_data_zdb_id
;

--update statistics low for table tmp_gene_pubcount;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/uniprot-zfinpub.txt' with delimiter as '	' null as '';'
create view uniprotZfinpub as
select geneid, szm_term_ont_id, dblink_acc_num,zdb_id, accession_no, 'Expression' as  cur_topic
from db_link, foreign_db_contains fdbc, foreign_db fdb, publication, tmp_gene_pubcount, expression_experiment, so_zfin_mapping, marker
where geneid=dblink_linked_recid
and szm_object_type = mrkr_type
and mrkr_zdb_id = geneid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and geneid=xpatex_gene_zdb_id
and xpatex_source_zdb_id=zdb_id
and pubcount <= 20
 and not exists (Select 'x' from clone
     	 		where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
and clone_problem_type = 'Chimeric')
and jtype='Journal'

union

select geneid, szm_term_ont_id, dblink_acc_num,zdb_id,accession_no,'GO' as  cur_topic
from db_link, foreign_db_contains fdbc, foreign_db fdb, publication, tmp_gene_pubcount, marker_go_term_evidence, so_zfin_mapping, marker
where geneid=dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
and szm_object_type = mrkr_type
and mrkr_zdb_id = geneid
and fdb.fdb_db_name = 'UniProtKB'
and geneid=mrkrgoev_mrkr_zdb_id
and mrkrgoev_source_zdb_id=zdb_id
and pubcount <= 20
and jtype='Journal'
union

select geneid, szm_term_ont_id, dblink_acc_num,zdb_id,accession_no,'Phenotype' as  cur_topic
--select count(*)
from db_link, foreign_db_contains fdbc, foreign_db fdb,  publication,tmp_gene_pubcount, feature_marker_relationship, genotype_feature, fish_experiment,  phenotype_experiment, phenotype_statement, figure, so_zfin_mapping, marker, fish, mutant_fast_search
where geneid=dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and szm_object_type = mrkr_type
and mrkr_zdb_id = geneid
and geneid = fmrel_mrkr_zdb_id
and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
and genofeat_geno_zdb_id = fish_genotype_zdb_id
and genox_zdb_id = phenox_genox_zdb_id
and fish_zdb_id = genox_fish_zdb_id
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount <= 20
and jtype='Journal'
and genox_is_std_or_generic_control = 't'
and phenos_tag!='normal'
and mfs_genox_zdb_id = genox_zdb_id

union

select geneid,szm_term_ont_id, dblink_acc_num,zdb_id,accession_no,'Phenotype' as cur_topic
--select count(*)
from phenotype_experiment, phenotype_statement, figure, tmp_gene_pubcount,foreign_db_contains fdbc, foreign_db fdb, db_link, publication, mutant_fast_search, marker, so_zfin_mapping
where phenox_genox_zdb_id = mfs_genox_zdb_id
and geneid = dblink_linked_recid
and szm_object_type = mrkr_type
and mrkr_zdb_id = geneid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and mfs_mrkr_zdb_id = geneid
and substring(mfs_mrkr_zdb_id from 1 for 8) in ('ZDB-GENE', 'ZDB-EFG-', 'ZDB-LNCR','ZDB-LINC','ZDB-TRNA','ZDB-RRNA','ZDB-MIRN','ZDB-SNOR','ZDB-NCRN','ZDB-PIRN')
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount <= 20
and jtype='Journal'
and phenos_tag!='normal'

union

select geneid,szm_term_ont_id,dblink_acc_num,zdb_id,accession_no,'Phenotype' as cur_topic
--select count(*)
from phenotype_experiment, phenotype_statement, figure, tmp_gene_pubcount, foreign_db_contains fdbc, foreign_db fdb, db_link, publication, mutant_fast_search, marker_relationship, marker, so_zfin_mapping
where phenox_genox_zdb_id = mfs_genox_zdb_id
and geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
and szm_object_type = mrkr_type
and mrkr_zdb_id = geneid
and fdb.fdb_db_name = 'UniProtKB'
and mfs_mrkr_zdb_id = mrel_mrkr_1_zdb_id
and substring(mfs_mrkr_zdb_id from 1 for 10) in ('ZDB-MRPHLN', 'ZDB-TALEN-', 'ZDB-CRISPR')
and mrel_mrkr_2_zdb_id = geneid
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount <= 20
and jtype = 'Journal'
and phenos_tag != 'normal'

union

select geneid, szm_term_ont_id, dblink_acc_num,zdb_id,accession_no,'GO' as  cur_topic
--select count(*)
from db_link, foreign_db_contains fdbc, foreign_db fdb,  publication, tmp_gene_pubcount, marker_go_term_evidence, marker, so_zfin_mapping
where geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and szm_object_type = mrkr_type
and mrkr_zdb_id = geneid
and geneid = mrkrgoev_mrkr_zdb_id
and mrkrgoev_source_zdb_id = zdb_id
and pubcount > 20
and jtype = 'Journal'

union

select geneid, szm_term_ont_id, dblink_acc_num,zdb_id,accession_no,'Phenotype' as  cur_topic
--select count(*)
from db_link, foreign_db_contains fdbc, foreign_db fdb,  publication, tmp_gene_pubcount, feature_marker_relationship, genotype_feature, fish, fish_experiment, phenotype_experiment, phenotype_statement, figure, marker, so_zfin_mapping, mutant_fast_search
where geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and geneid = fmrel_mrkr_zdb_id
and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
and genofeat_geno_zdb_id = fish_genotype_zdb_id
and genox_zdb_id = phenox_genox_zdb_id
and fish_zdb_id = genox_fish_zdb_id
and szm_object_type = mrkr_type
and mrkr_zdb_id = geneid
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount > 20
and jtype='Journal'
and genox_is_std_or_generic_control = 't'
and phenos_tag!='normal'
and mfs_genox_zdb_id = genox_zdb_id

union

select geneid, szm_term_ont_id,dblink_acc_num,zdb_id,accession_no,'Phenotype' as cur_topic
--select count(*)
from phenotype_experiment, phenotype_statement, figure, tmp_gene_pubcount, foreign_db_contains fdbc, foreign_db fdb, db_link, publication, mutant_fast_search, marker, so_zfin_mapping
where phenox_genox_zdb_id = mfs_genox_zdb_id
and geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id=fdb.fdb_db_pk_id
and fdb.fdb_db_name = 'UniProtKB'
and mfs_mrkr_zdb_id = geneid
and szm_object_type = mrkr_type
and mrkr_zdb_id = geneid
and substring(mfs_mrkr_zdb_id from 1 for 8) in ('ZDB-GENE', 'ZDB-EFG-', 'ZDB-LNCR','ZDB-LINC','ZDB-TRNA','ZDB-RRNA','ZDB-MIRN','ZDB-SNOR','ZDB-NCRN','ZDB-PIRN')
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount > 20
and jtype='Journal'
and phenos_tag !='normal'

union

select geneid, szm_term_ont_id,dblink_acc_num,zdb_id,accession_no,'Phenotype' as cur_topic
--select count(*)
from phenotype_experiment, phenotype_statement, figure, tmp_gene_pubcount,foreign_db_contains fdbc, foreign_db fdb, db_link, publication,mutant_fast_search, marker_relationship, marker, so_zfin_mapping
where phenox_genox_zdb_id = mfs_genox_zdb_id
and geneid = dblink_linked_recid
and dblink_fdbcont_zdb_id = fdbc.fdbcont_zdb_id
and fdbc.fdbcont_fdb_db_id = fdb.fdb_db_pk_id
and szm_object_type = mrkr_type
and mrkr_zdb_id = geneid
and fdb.fdb_db_name = 'UniProtKB'
and mfs_mrkr_zdb_id = mrel_mrkr_1_zdb_id
and substring(mfs_mrkr_zdb_id from 1 for 10) in ('ZDB-MRPHLN', 'ZDB-TALEN-', 'ZDB-CRISPR')
and mrel_mrkr_2_zdb_id = geneid
and phenos_phenox_pk_id = phenox_pk_id
and phenox_fig_zdb_id = fig_zdb_id
and fig_source_zdb_id = zdb_id
and pubcount > 20
and phenos_tag !='normal'
and jtype='Journal';

\copy (select * from uniprotZfinpub) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/uniprot-zfinpub.txt' with delimiter as '	' null as '';
drop view uniprotZfinpub;

-- download file Case 4693 as reuqested by uniprot
\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/zfinpubs.txt' with delimiter as '	' null as '';'
create view zfinpubs as
select zdb_id, accession_no, authors,title,jrnl_name,extract(year from pub_date),pub_volume,pub_pages
 from publication, journal
 where pub_jrnl_zdb_id = jrnl_zdb_id
   and jtype = 'Journal'
;
\copy (select * from zfinpubs) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/zfinpubs.txt' with delimiter as '	' null as '';
drop view zfinpubs;

create temp table tmp_features (feature_id text, term_o_id text, f_abbrev text,
                               f_name text,ftypedisp text, mutagen text,
                               mutagee text, construct_id text, construct_name text, construct_so_id text,
                               created_by_zdb_id text, created_by_name text)
;

-- a list of all features ordered by abbreviation (case insensitive)
insert into tmp_features (feature_id, term_o_id, f_abbrev, f_name, ftypedisp, mutagen, mutagee)
select feature_zdb_id, szm_term_ont_id, feature_abbrev, feature_name, ftrtype_type_display, featassay_mutagen,featassay_mutagee
from feature, feature_type, feature_assay, so_zfin_mapping
where feature_type =ftrtype_name
 and szm_object_type = feature_type
 and feature_zdb_id = featassay_feature_zdb_id
and feature_Type not in ('TRANSGENIC_INSERTION')
and featassay_mutagen not in ('TALEN', 'CRISPR', 'DNA and TALEN', 'DNA and CRISPR')
order by lower(feature_abbrev);

insert into tmp_features (feature_id, term_o_id, f_abbrev, f_name, ftypedisp, mutagen, mutagee, created_by_zdb_id, created_by_name)
select feature_zdb_id, szm_term_ont_id, feature_abbrev, feature_name, ftrtype_type_display, featassay_mutagen,featassay_mutagee,fmrel_mrkr_zdb_id,mrkr_name
from feature, feature_type, feature_assay, so_zfin_mapping, feature_marker_relationship, marker
where feature_type =ftrtype_name
 and szm_object_type = feature_type
 and feature_zdb_id = featassay_feature_zdb_id
and feature_Type not in ('TRANSGENIC_INSERTION')
and featassay_mutagen in ('TALEN', 'CRISPR', 'DNA and TALEN', 'DNA and CRISPR')
and fmrel_type = 'created by'
and feature_zdb_id = fmrel_ftr_zdb_id
and mrkr_zdb_id = fmrel_mrkr_zdb_id
order by lower(feature_abbrev);

insert into tmp_features (feature_id, term_o_id, f_abbrev, f_name, ftypedisp, mutagen, mutagee, construct_id, construct_name)
select feature_zdb_id, szm_term_ont_id, feature_abbrev, feature_name, ftrtype_type_display, featassay_mutagen,featassay_mutagee, mrkr_zdb_id, mrkr_name
from feature, feature_type, feature_assay, so_zfin_mapping, feature_marker_relationship, marker
where feature_type =ftrtype_name
 and szm_object_type = feature_type
 and feature_zdb_id = featassay_feature_zdb_id
 and feature_zdb_id = fmrel_ftr_zdb_id
 and fmrel_mrkr_zdb_id = mrkr_zdb_id
 and mrkr_type in ('TGCONSTRCT','GTCONSTRCT','PTCONSTRCT','ETCONSTRCT')
and feature_Type in ('TRANSGENIC_INSERTION')
and fmrel_type != 'is allele of'
and featassay_mutagen not in ('TALEN', 'CRISPR', 'DNA and TALEN', 'DNA and CRISPR')
order by lower(feature_abbrev);

insert into tmp_features (feature_id, term_o_id, f_abbrev, f_name, ftypedisp, mutagen, mutagee, construct_id, construct_name, created_by_zdb_id, created_by_name)
select feature_zdb_id, szm_term_ont_id, feature_abbrev, feature_name, ftrtype_type_display, featassay_mutagen,featassay_mutagee, construct.mrkr_zdb_id, construct.mrkr_name, createdby.fmrel_mrkr_zdb_id, str.mrkr_name
from feature, feature_type, feature_assay, so_zfin_mapping, feature_marker_relationship cst, marker construct, feature_marker_relationship createdby, marker str
where feature_type =ftrtype_name
 and szm_object_type = feature_type
 and feature_zdb_id = featassay_feature_zdb_id
 and feature_zdb_id = cst.fmrel_ftr_zdb_id
 and cst.fmrel_mrkr_zdb_id = construct.mrkr_zdb_id
 and construct.mrkr_type in ('TGCONSTRCT','GTCONSTRCT','PTCONSTRCT','ETCONSTRCT')
and feature_Type in ('TRANSGENIC_INSERTION')
and cst.fmrel_type != 'is allele of'
and featassay_mutagen in ('TALEN', 'CRISPR', 'DNA and TALEN', 'DNA and CRISPR')
and createdby.fmrel_type = 'created by'
and feature_zdb_id = createdby.fmrel_ftr_zdb_id
and str.mrkr_zdb_id = createdby.fmrel_mrkr_zdb_id
order by lower(feature_abbrev);

insert into tmp_features (feature_id, term_o_id, f_abbrev, f_name, ftypedisp, mutagen, mutagee, construct_id, construct_name)
select feature_zdb_id, szm_term_ont_id, feature_abbrev, feature_name, ftrtype_type_display, featassay_mutagen,featassay_mutagee, mrkr_zdb_id, mrkr_name
from feature, feature_type, feature_assay, so_zfin_mapping, feature_marker_relationship, marker
where feature_type =ftrtype_name
 and szm_object_type = feature_type
 and feature_zdb_id = featassay_feature_zdb_id
 and feature_zdb_id = fmrel_ftr_zdb_id
 and fmrel_mrkr_zdb_id = mrkr_zdb_id
 and mrkr_type  in ('TGCONSTRCT','GTCONSTRCT','PTCONSTRCT','ETCONSTRCT')
and feature_Type in ('TRANSGENIC_INSERTION')
and fmrel_type = 'is allele of'
and featassay_mutagen not in ('TALEN', 'CRISPR', 'DNA and TALEN', 'DNA and CRISPR')
order by lower(feature_abbrev);

insert into tmp_features (feature_id, term_o_id, f_abbrev, f_name, ftypedisp, mutagen, mutagee, construct_id, construct_name, created_by_zdb_id, created_by_name)
select feature_zdb_id, szm_term_ont_id, feature_abbrev, feature_name, ftrtype_type_display, featassay_mutagen,featassay_mutagee, construct.mrkr_zdb_id, construct.mrkr_name, createdby.fmrel_mrkr_zdb_id, str.mrkr_name
from feature, feature_type, feature_assay, so_zfin_mapping, feature_marker_relationship cst, marker construct, feature_marker_relationship createdby, marker str
where feature_type =ftrtype_name
 and szm_object_type = feature_type
 and feature_zdb_id = featassay_feature_zdb_id
 and feature_zdb_id = cst.fmrel_ftr_zdb_id
 and cst.fmrel_mrkr_zdb_id = construct.mrkr_zdb_id
 and construct.mrkr_type  in ('TGCONSTRCT','GTCONSTRCT','PTCONSTRCT','ETCONSTRCT')
and feature_Type in ('TRANSGENIC_INSERTION')
and cst.fmrel_type = 'is allele of'
and featassay_mutagen in ('TALEN', 'CRISPR', 'DNA and TALEN', 'DNA and CRISPR')
and createdby.fmrel_type = 'created by'
and feature_zdb_id = createdby.fmrel_ftr_zdb_id
and str.mrkr_zdb_id = createdby.fmrel_mrkr_zdb_id
order by lower(feature_abbrev);

update tmp_features
  set construct_so_id = (select szm_term_ont_id from so_zfin_mapping
      		      		where get_obj_type(construct_id) = szm_object_type)
 where construct_id is not null;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/features.txt' with delimiter as '	' null as '';'
\copy (select * from tmp_features) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/features.txt' with delimiter as '	' null as '';

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/snpData.txt' with delimiter as '	' null as '';'
create view snpData as
select mrkr_zdb_id, mrkr_abbrev, snpd_rs_acc_num, snpdattr_pub_zdb_id
  from marker, snp_download, snp_download_attribution
 where mrkr_zdb_id = snpd_mrkr_zdb_id
 and snpdattr_snpd_pk_id = snpd_pk_id;
\copy (select * from snpData) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/snpData.txt' with delimiter as '	' null as '';
drop view snpData;


CREATE TEMP TABLE tmp_mutation_details(
  tmp_feat_zdb_id text,
  dna_change_so_id text,
  dna_ref_nucleotide text,
  dna_mut_nucleotide text,
  dna_bp_added bigint,
  dna_bp_removed bigint,
  dna_position_start bigint,
  dna_position_end bigint,
  dna_reference_seq text,
  dna_localization_name text,
  dna_localization_so_id text,
  dna_localization_exon bigint,
  dna_localization_intron bigint,
  transcript_consequence_name text,
  transcript_consequence_so_id text,
  transcript_consequence_exon bigint,
  transcript_consequence_intron bigint,
  protein_consequence_name text,
  protein_consequence_so_id text,
  protein_ref_aa text,
  protein_mut_aa text,
  protein_aa_added bigint,
  protein_aa_removed bigint,
  protein_position_start bigint,
  protein_position_end bigint,
  protein_reference_seq text
);

CREATE TEMP TABLE tmp_term_names_and_ids(
  zdb_id text,
  ont_id text,
  display text
);

INSERT INTO tmp_term_names_and_ids
  SELECT mdcv_term_zdb_id, term_ont_id, mdcv_term_display_name
  FROM mutation_detail_controlled_vocabulary
  INNER JOIN term ON term_zdb_id = mdcv_term_zdb_id;

INSERT INTO tmp_mutation_details
  SELECT
    feature_zdb_id,
    dna_term.ont_id,
    substring(dna_term.display from 1 for 1),
    substring(dna_term.display from 4 for 1),
    fdmd_number_additional_dna_base_pairs,
    fdmd_number_removed_dna_base_pairs,
    fdmd_dna_position_start,
    fdmd_dna_position_end,
    dna_db.fdb_db_display_name || ':' || fdmd_dna_sequence_of_reference_accession_number,
    localization_term.display,
    localization_term.ont_id,
    fdmd_exon_number,
    fdmd_intron_number,
    transcript_term.display,
    transcript_term.ont_id,
    ftmd_exon_number,
    ftmd_intron_number,
    protein_term.display,
    protein_term.ont_id,
    wt_aa.display,
    mut_aa.display,
    fpmd_number_amino_acids_added,
    fpmd_number_amino_acids_removed,
    fpmd_protein_position_start,
    fpmd_protein_position_end,
    prot_db.fdb_db_display_name || ':' || fpmd_sequence_of_reference_accession_number
  FROM feature
  LEFT OUTER JOIN feature_dna_mutation_detail ON feature_zdb_id = fdmd_feature_zdb_id
  LEFT OUTER JOIN tmp_term_names_and_ids dna_term ON dna_term.zdb_id = fdmd_dna_mutation_term_zdb_id
  LEFT OUTER JOIN foreign_db_contains dna_dbc ON fdmd_fdbcont_zdb_id = dna_dbc.fdbcont_zdb_id
  LEFT OUTER JOIN foreign_db dna_db ON dna_db.fdb_db_pk_id = dna_dbc.fdbcont_fdb_db_id
  LEFT OUTER JOIN tmp_term_names_and_ids localization_term ON localization_term.zdb_id = fdmd_gene_localization_term_zdb_id
  LEFT OUTER JOIN feature_transcript_mutation_detail ON feature_zdb_id = ftmd_feature_zdb_id
  LEFT OUTER JOIN tmp_term_names_and_ids transcript_term ON transcript_term.zdb_id = ftmd_transcript_consequence_term_zdb_id
  LEFT OUTER JOIN feature_protein_mutation_detail ON feature_zdb_id = fpmd_feature_zdb_id
  LEFT OUTER JOIN tmp_term_names_and_ids protein_term ON protein_term.zdb_id = fpmd_protein_consequence_term_zdb_id
  LEFT OUTER JOIN tmp_term_names_and_ids wt_aa ON wt_aa.zdb_id = fpmd_wt_protein_term_zdb_id
  LEFT OUTER JOIN tmp_term_names_and_ids mut_aa ON mut_aa.zdb_id = fpmd_mutant_or_stop_protein_term_zdb_id
  LEFT OUTER JOIN foreign_db_contains prot_dbc ON fpmd_fdbcont_zdb_id = prot_dbc.fdbcont_zdb_id
  LEFT OUTER JOIN foreign_db prot_db ON prot_db.fdb_db_pk_id = prot_dbc.fdbcont_fdb_db_id;

\echo ''<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/features-affected-genes.txt' with delimiter as '	' null as '';'
create view featuresAffectedGenes as
  SELECT
    feature_zdb_id,
    a.szm_term_ont_id,
    feature_name,
    mrkr_abbrev,
    mrkr_zdb_id,
    b.szm_term_ont_id as id2,
    fmrel_type,
    feature_type,
    dna_change_so_id,
    dna_ref_nucleotide,
    dna_mut_nucleotide,
    dna_bp_added,
    dna_bp_removed,
    dna_position_start,
    dna_position_end,
    dna_reference_seq,
    dna_localization_name,
    dna_localization_so_id,
    dna_localization_exon,
    dna_localization_intron,
    transcript_consequence_name,
    transcript_consequence_so_id,
    transcript_consequence_exon,
    transcript_consequence_intron,
    protein_consequence_name,
    protein_consequence_so_id,
    protein_ref_aa,
    protein_mut_aa,
    protein_aa_added,
    protein_aa_removed,
    protein_position_start,
    protein_position_end,
    protein_reference_seq
  FROM feature, feature_marker_relationship,marker, marker_type_group_member, so_zfin_mapping a, so_zfin_mapping b, tmp_mutation_details
  WHERE fmrel_ftr_zdb_id = feature_zdb_id
  AND mrkr_zdb_id = fmrel_mrkr_zdb_id
  AND a.szm_object_type = feature_type
  AND b.szm_objecT_type = mrkr_type
  AND mrkr_type =mtgrpmem_mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM_AND_NTR' AND (
    (feature_type IN ('POINT_MUTATION', 'DELETION', 'INSERTION','COMPLEX_SUBSTITUTION','SEQUENCE_VARIANT',
                      'UNSPECIFIED','TRANSGENIC_INSERTION', 'INDEL') AND fmrel_type ='is allele of') OR
    (feature_type IN ('TRANSLOC', 'INVERSION') AND fmrel_type IN ('is allele of', 'markers moved')) OR
    (feature_type IN ('DEFICIENCY') AND fmrel_type IN ('is allele of','markers missing')))
  AND tmp_feat_zdb_id = feature_zdb_id
  ORDER BY LOWER(feature_name);
\copy (select * from featuresAffectedGenes) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/features-affected-genes.txt' with delimiter as '	' null as '';
drop view featuresAffectedGenes;

DROP TABLE tmp_term_names_and_ids;
DROP TABLE tmp_mutation_details;


\echo 'generating clean phenotype download' ;


\echo 'Create tmp_dumpPheno temp table'
create temp table tmp_dumpCleanPheno as
 select  psg_id as phenos_id,
         pg_id as phenox_id,
         mfs_mrkr_zdb_id as gene_Zdb_id,
         '                                                                     ' as fish_full_name,
                 pg_fig_zdb_id as fig_id,
                 fig_source_zdb_id as pub_id,
                 genox_zdb_id as id,
                 genox_fish_zdb_id as fish_id,
                 '' as mo_id,
                 pg_start_stg_zdb_id as stage_start_id,
                 pg_end_stg_zdb_id as stage_end_id,
                 genox_Zdb_id as genox_id
   from fish_Experiment,
        phenotype_source_generated,
        phenotype_observation_generated,
        mutant_fasT_search, figure
    where pg_genox_zdb_id = genox_zdb_id
    and psg_pg_id = pg_id
    and mfs_genox_zdb_id = genox_zdb_id
    and fig_zdb_id = pg_fig_zdb_id
    and substring(mfs_mrkr_zdb_id from 1 for 8) in ('ZDB-GENE', 'ZDB-EFG-', 'ZDB-LNCR','ZDB-LINC','ZDB-TRNA','ZDB-RRNA','ZDB-MIRN','ZDB-SNOR','ZDB-NCRN','ZDB-PIRN')
    and not exists (Select 'x' from fish_str where fishstr_fish_Zdb_id = genox_fish_Zdb_id)
union
 select  psg_id,
         pg_id as phenox_id,
         mfs_mrkr_zdb_id as gene_Zdb_id,
         '                                                                     ' as fish_full_name,
         pg_fig_zdb_id as fig_id,
         fig_source_zdb_id,
                 genox_zdb_id,
                 genox_fish_zdb_id,
                 fishstr_str_zdb_id,
                 pg_start_stg_zdb_id as stage_start_id,
                 pg_end_stg_zdb_id as stage_end_id,
                 genox_zdb_id
   from fish_Experiment,
        phenotype_source_generated,
        phenotype_observation_generated,
        fish_str,
        mutant_fast_Search, figure
    where pg_genox_zdb_id = genox_zdb_id
    and pg_fig_zdb_id = fig_zdb_id
    and psg_pg_id = pg_id
    and genox_fish_zdb_id = fishstr_fish_Zdb_id
    and mfs_genox_zdb_id = genox_zdb_id
    and substring(mfs_mrkr_zdb_id from 1 for 8) in ('ZDB-GENE', 'ZDB-EFG-', 'ZDB-LNCR','ZDB-LINC','ZDB-TRNA','ZDB-RRNA','ZDB-MIRN','ZDB-SNOR','ZDB-NCRN','ZDB-PIRN')
;

\echo 'update gene_display name'
update tmp_dumpCleanPheno
  set fish_full_name = (select fish_full_name
      			  	  from fish
				  where fish_zdb_id = fish_id);



\echo 'update gene_display name'
update tmp_dumpCleanPheno
  set fish_full_name = (select fish_full_name
      			  	  from fish
				  where fish_zdb_id = fish_id);

--remove normals for clean phenotype download.
--TODO: clean this up so that we don't reuse temp tables for different files.
delete from tmp_phenotype_statement
  where quality_tag = 'normal';

\echo '\copy (select * from phenoGeneCleanDataFish) phenoGeneCleanData.txt'
create view phenoGeneCleanDataFish as
  select phenos_id,
		mrkr_abbrev,
		mrkr_zdb_id,
   		tps.asubterm_ont_id,
		tps.asubterm_name,
	 tps.arelationship_id,
	 tps.arelationship_name,
	 tps.asuperterm_ont_id,
	 tps.asuperterm_name,
	 tps.quality_id,
	 tps.quality_name,
	 tps.quality_tag,
	 tps.bsubterm_ont_id,
	 tps.bsubterm_name,
	 tps.brelationship_id,
	 tps.brelationship_name,
	 tps.bsuperterm_ont_id,
	 tps.bsuperterm_name,
	 fish_id,
	 fish_full_name,
	 stage_start_id,
	 stage_end_id,
	 genox_id,
	 pub_id,
	 fig_id
  From tmp_dumpCleanPheno, tmp_phenotype_statement tps, marker
  where tps.phenos_pk_id = phenos_id
and mrkr_Zdb_id = gene_zdb_id
  order by mrkr_abbrev;
\copy (select * from phenoGeneCleanDataFish) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/phenoGeneCleanData_fish.txt' with delimiter as '	' null as '';  
drop view phenoGeneCleanDataFish;

\echo '\copy (select * from crisprFasta) crispr fasta file' 
create view crisprFasta as
select '>lcl|', mrkr_zdb_id, mrkr_name||'|' as text2, '
'||seq_sequence as text3
from marker, marker_sequence
 where mrkr_zdb_id = seq_mrkr_zdb_id
 and mrkr_zdb_id like 'ZDB-CRISPR%';
\copy (select * from crisprFasta) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/crispr_fasta.fa'
drop view crisprFasta; 

\echo '\copy (select * from talenFasta) talen fasta file'
create view talenFasta as
select '>lcl|',mrkr_zdb_id||' sequence1' as text2, mrkr_name||'|' as text3, '
'||seq_sequence as test4
from marker, marker_sequence
 where mrkr_zdb_id = seq_mrkr_zdb_id
 and mrkr_zdb_id like 'ZDB-TALEN%'
union
select '>lcl|',mrkr_zdb_id||' sequence2',mrkr_name||'|', '
'||seq_sequence_2
from marker, marker_sequence
 where mrkr_zdb_id = seq_mrkr_zdb_id
 and mrkr_zdb_id like 'ZDB-TALEN%';
\copy (select * from talenFasta) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/talen_fasta.fa'
drop view talenFasta;

\echo '\copy (select * from fishModelDisease) fish_model_disease.txt'
create view fishModelDisease as
SELECT genox_fish_zdb_id,
       genox_exp_zdb_id,
       CASE
         WHEN genox_fish_zdb_id IS NOT NULL THEN 'is_a_model'
         ELSE ' '
       END,
       term_ont_id,
       term_name,
       dat_source_zdb_id,
       accession_no,
       CASE dat_evidence_term_zdb_id
         WHEN 'ZDB-TERM-170419-151' THEN 'IC, ECO:ECO:0000305'
         WHEN 'ZDB-TERM-170419-33' THEN 'TAS, ECO:ECO:0000304'
         ELSE dat_evidence_term_zdb_id
       END
FROM disease_annotation_model
JOIN disease_annotation
  ON damo_dat_Zdb_id=dat_zdb_id
JOIN publication
  ON dat_source_zdb_id = zdb_id
JOIN term
  ON dat_term_zdb_id = term_zdb_id
JOIN fish_experiment
  ON genox_zdb_id = damo_genox_zdb_id
;
\copy (select * from fishModelDisease) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/fish_model_disease.txt' with delimiter as '	' null as '';
drop view fishModelDisease;

\echo 'experiment details file'
create view experimentDetails as
  select expcond_exp_zdb_id, z.term_ont_id, a.term_ont_id as id2, g.term_ont_id as id3, c.term_ont_id as id4, t.term_ont_id as id5
    from experiment_condition
    full outer join term z on expcond_zeco_Term_Zdb_id = z.term_zdb_id
    full outer join term a on expcond_ao_Term_zdb_id = a.term_Zdb_id
    full outer join term g on expcond_go_cc_term_Zdb_id = g.term_Zdb_id
    full outer join term c on expcond_chebi_term_zdb_id = c.term_Zdb_id
    full outer join term t on expcond_taxon_Term_zdb_id = t.term_Zdb_id
;
\copy (select * from experimentDetails) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/experiment_details.txt' with delimiter as '	' null as '';
drop view experimentDetails;

\echo 'inno/pheno construct report'
create view innophenoconstructs as
select fmrel_mrkr_zdb_id, mrkr_name, fmrel_type, fmrel_ftr_zdb_id, feature_name
  from feature, marker, feature_marker_relationship
 where feature_zdb_id = fmrel_ftr_zdb_id
 and fmrel_mrkr_Zdb_id = mrkr_zdb_id
 and fmrel_type in ('contains innocuous sequence feature','contains phenotypic sequence feature');
\copy (select * from innophenoconstructs) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/innophenoconstructs.txt' with delimiter as '	' null as '';
drop view innophenoconstructs;

-- generate a file to map Disease to Genes via Orthology
\echo 'disease_gene_ortholog.txt'
create view diseaseGeneOrtholog as
select ortho_zebrafish_gene_zdb_id, mrkr_abbrev, ortho_other_species_ncbi_gene_id, ortho_other_species_symbol, term_name, term_zdb_id, omimp_name, omimp_omim_id
  from ortholog, marker, omim_phenotype, omimp_termxref_mapping, term_xref, term
 where ortho_other_species_taxid = 9606
   and ortho_zebrafish_gene_zdb_id = mrkr_zdb_id
   and omimp_ortho_zdb_id = ortho_zdb_id
   and otm_omimp_id = omimp_pk_id
   and tx_pk_id = otm_tx_id
   and term_zdb_id = tx_term_zdb_id
union
select ortho_zebrafish_gene_zdb_id, mrkr_abbrev, ortho_other_species_ncbi_gene_id, ortho_other_species_symbol, '', '', omimp_name, omimp_omim_id
  from ortholog, marker, omim_phenotype
 where ortho_other_species_taxid = 9606
   and ortho_zebrafish_gene_zdb_id = mrkr_zdb_id
   and omimp_ortho_zdb_id = ortho_zdb_id
   and not exists(select 'x' from omimp_termxref_mapping, term_xref, term
                   where otm_omimp_id = omimp_pk_id
                     and tx_pk_id = otm_tx_id
                     and term_zdb_id = tx_term_zdb_id)
union
select ortho_zebrafish_gene_zdb_id, mrkr_abbrev, ortho_other_species_ncbi_gene_id, ortho_other_species_symbol, '', '', omimp_name, ''
  from ortholog, marker, omim_phenotype
 where ortho_other_species_taxid = 9606
   and ortho_zebrafish_gene_zdb_id = mrkr_zdb_id
   and omimp_ortho_zdb_id = ortho_zdb_id
   and omimp_omim_id is null
order by 2, 4, 5, 7;
\copy (select * from diseaseGeneOrtholog) to '<!--|ROOT_PATH|-->/server_apps/data_transfer/Downloads/downloadsStaging/disease_gene_ortholog.txt' with delimiter as '	' null as '';
drop view diseaseGeneOrtholog;

commit work;
