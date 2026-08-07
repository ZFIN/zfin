--liquibase formatted sql
--changeset cmpich:0050-ZFIN-10330-moens-hcr-expression-records

-- ZFIN-10330 / ZFINHELP-5462 : Moens lab HCR in situ expression records.
--
-- Turns the cleaned staging rows from migration 0040 into real expression data and
-- then throws the staging away. Runs after org.zfin.figure.MoensHcrImageLoad has
-- created the 102 figures/images, which is what populates moens_hcr_image_load_map.
--
-- What 113 staging rows become:
--     1 fish_experiment      -- fh474Tg (AB) under standard conditions
--    34 expression_experiment2  -- one per gene (fish/assay/pub are uniform)
--   103 expression_figure_stage -- one per gene x figure x stage
--   113 expression_result2      -- one per staging row (superterm + subterm)
--
-- ASSAY. The spreadsheet says "HCR in situ / MMO_0000643", but MMO:0000643 is the
-- generic parent term "in situ expression assay" and no row of expression_pattern_assay
-- carries it -- xpatex_assay_name is a FK to that table, so the source value cannot be
-- stored as given. HCR is an RNA in situ hybridization method, so these load under the
-- existing "mRNA in situ hybridization" (MMO:0000658, ISH). Change the single literal
-- below if the curator wants a dedicated HCR assay added to expression_pattern_assay
-- instead; that is a wider change (curation dropdown, display order, Alliance export).
--
-- Sheet rows 19, 21 and 29 are absent throughout: their spreadsheet image filename
-- matched no file on disk and the curator is assigning a different image. They were
-- never staged, so nothing here has to exclude them explicitly.

-- The standard (unmanipulated) environment for the paper's fish. ZDB-EXP-041102-1 is
-- the site-wide "_Standard" experiment; genox_is_standard is filled in by a trigger.
insert into fish_experiment (genox_zdb_id, genox_exp_zdb_id, genox_fish_zdb_id)
select get_id_and_insert_active_data('GENOX'), 'ZDB-EXP-041102-1', 'ZDB-FISH-260717-2'
where not exists (
  select 1 from fish_experiment
  where genox_fish_zdb_id = 'ZDB-FISH-260717-2'
    and genox_exp_zdb_id = 'ZDB-EXP-041102-1'
);

-- One experiment per gene. Fish, assay and publication are the same for every staging
-- row, so gene is the only thing that varies across unique_expression_experiment2.
-- An AFTER trigger attributes both the experiment and the gene to the publication.
insert into expression_experiment2
  (xpatex_zdb_id, xpatex_assay_name, xpatex_gene_zdb_id, xpatex_genox_zdb_id, xpatex_source_zdb_id)
select get_id_and_insert_active_data('XPAT'),
       'mRNA in situ hybridization',
       genes.mhel_gene_zdb_id,
       genox.genox_zdb_id,
       'ZDB-PUB-260717-17'
from (select distinct mhel_gene_zdb_id from moens_hcr_expression_load) genes
cross join (
  select genox_zdb_id from fish_experiment
  where genox_fish_zdb_id = 'ZDB-FISH-260717-2'
    and genox_exp_zdb_id = 'ZDB-EXP-041102-1'
) genox;

-- One figure/stage per gene x figure x stage. Each annotation is a single imaged
-- timepoint, so start and end stage are the same stage.
insert into expression_figure_stage
  (efs_xpatex_zdb_id, efs_fig_zdb_id, efs_start_stg_zdb_id, efs_end_stg_zdb_id)
select distinct xpatex.xpatex_zdb_id, map.mhilm_fig_zdb_id, stg.stg_zdb_id, stg.stg_zdb_id
from moens_hcr_expression_load load
join moens_hcr_image_load_map map on map.mhilm_image_stem = load.mhel_image_stem
join stage stg on stg.stg_obo_id = load.mhel_zfs_id
join expression_experiment2 xpatex on xpatex.xpatex_gene_zdb_id = load.mhel_gene_zdb_id
                                  and xpatex.xpatex_source_zdb_id = 'ZDB-PUB-260717-17';

-- One result per staging row. 0040 already split multi-structure and multi-gene cells,
-- so every row here is exactly one superterm/subterm pair.
insert into expression_result2
  (xpatres_efs_id, xpatres_expression_found, xpatres_superterm_zdb_id, xpatres_subterm_zdb_id)
select efs.efs_pk_id, load.mhel_expression_found, super.term_zdb_id, sub.term_zdb_id
from moens_hcr_expression_load load
join moens_hcr_image_load_map map on map.mhilm_image_stem = load.mhel_image_stem
join stage stg on stg.stg_obo_id = load.mhel_zfs_id
join expression_experiment2 xpatex on xpatex.xpatex_gene_zdb_id = load.mhel_gene_zdb_id
                                  and xpatex.xpatex_source_zdb_id = 'ZDB-PUB-260717-17'
join expression_figure_stage efs on efs.efs_xpatex_zdb_id = xpatex.xpatex_zdb_id
                                and efs.efs_fig_zdb_id = map.mhilm_fig_zdb_id
                                and efs.efs_start_stg_zdb_id = stg.stg_zdb_id
join term super on super.term_ont_id = load.mhel_superterm_zfa
join term sub on sub.term_ont_id = load.mhel_subterm_zfa;

-- Both staging tables are deliberately left in place here; migration 0060 drops them
-- once the records above have been checked. Keeping them past this changeset means a
-- bad load can be deleted and rebuilt from the same source rows.
