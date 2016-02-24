select xpatex_genox_zdb_id as genox_id, efs_fig_zdb_id as fig_id, efs_start_stg_zdb_id as start_id, efs_end_stg_zdb_id as end_id, 'f' as old_data
  from expression_experiment2, expression_figure_stage
  where xpatex_zdb_id = efs_xpatex_zdb_id
  and exists (select 'x' from expression_phenotype_term, expression_result2
              where xpatres_efs_id = efs_pk_id
              and xpatres_pk_id = ept_xpatres_id)
union
select phenox_genox_zdb_id, phenox_fig_zdb_id, phenox_start_Stg_zdb_id, phenox_end_stg_zdb_id,
     case 
      when phenox_created_date < '2016-02-15 00:00:00' then 't'
      else 'f'
     end as old_data
  from phenotype_experiment
into temp tmp_pheno;

select count(*) from tmp_pheno
 where old_data = 't';

insert into phenotype_source_generated_temp (pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id, pg_pre_eap_phenotype)
select genox_id, fig_id, starT_id, end_id, old_data
  from tmp_pheno;

insert into phenotype_observation_generated_temp (psg_pg_id, psg_mrkr_Zdb_id, psg_mrkr_Relation, psg_e1a_zdb_id, psg_e1_relation_name,
                                                  psg_e1b_zdb_id, psg_e2a_zdb_id, psg_e2_relation_name,
                                                  psg_e2b_zdb_id, psg_tag, psg_quality_zdb_id)
  select pg_id, xpatex_gene_zdb_id, "expression", xpatres_superterm_zdb_id, ept_relational_term, xpatres_subterm_zdb_id, "","","",ept_tag, ept_quality_term_zdb_id
    from expression_experiment2, expression_figure_stage, expression_result2, expression_phenotype_term, phenotype_source_generated_temp
    where xpatex_zdb_id = efs_xpatex_zdb_id
    and efs_pk_id = xpatres_efs_id
    and ept_xpatres_id = xpatres_pk_id
    and pg_genox_zdb_id = xpatex_genox_Zdb_id
    and pg_fig_zdb_id = efs_fig_Zdb_id
    and pg_start_Stg_zdb_id = efs_start_Stg_zdb_id
    and pg_end_stg_zdb_id = efs_end_stg_Zdb_id
    and xpatex_gene_zdb_id is not null;

insert into phenotype_observation_generated_temp (psg_pg_id, psg_mrkr_Zdb_id,  psg_mrkr_Relation,psg_e1a_zdb_id, psg_e1_relation_name,
                                                  psg_e1b_zdb_id, psg_e2a_zdb_id, psg_e2_relation_name,
                                                  psg_e2b_zdb_id, psg_tag, psg_quality_zdb_id)
  select pg_id, xpatex_atb_zdb_id, "labeling",xpatres_superterm_zdb_id, ept_relational_term, xpatres_subterm_zdb_id, "","","",ept_tag,ept_quality_term_zdb_id
    from expression_experiment2, expression_figure_stage, expression_result2, expression_phenotype_term, phenotype_source_generated_temp
    where xpatex_zdb_id = efs_xpatex_zdb_id
    and efs_pk_id = xpatres_efs_id
    and ept_xpatres_id = xpatres_pk_id
    and pg_genox_zdb_id = xpatex_genox_Zdb_id
    and pg_fig_zdb_id = efs_fig_Zdb_id
    and pg_start_Stg_zdb_id = efs_start_Stg_zdb_id
    and pg_end_stg_zdb_id = efs_end_stg_Zdb_id
    and xpatex_gene_zdb_id is null;

insert into phenotype_observation_generated_temp (psg_pg_id,  psg_e1a_zdb_id, psg_e1_relation_name,
                                                  psg_e1b_zdb_id, psg_e2a_zdb_id, psg_e2_relation_name,
                                                  psg_e2b_zdb_id, psg_tag, psg_quality_zdb_id)
  select pg_id, phenos_entity_1_superterm_zdb_id, "part of", phenos_entity_1_subterm_Zdb_id, 
         phenos_entity_2_superterm_zdb_id, "part of", phenos_entity_2_subterm_Zdb_id,
         phenos_tag, phenos_quality_zdb_id
    from phenotype_experiment, phenotype_statement, phenotype_source_generated_temp
    where phenox_genox_Zdb_id = pg_genox_Zdb_id
    and phenox_fig_Zdb_id =pg_fig_zdb_id
    and phenox_start_Stg_zdb_id =pg_start_Stg_zdb_id
    and phenox_end_Stg_zdb_id = pg_end_stg_zdb_id
    and phenos_phenox_pk_id = phenox_pk_id;

update phenotype_observation_generated_temp
  set psg_mrkr_abbrev = (
    select case mrkr_type
      when 'GENE' then "<i>"||mrkr_abbrev||"</i>"
      else mrkr_abbrev
    end
    from marker
    where psg_mrkr_zdb_id = mrkr_zdb_id
  );

update phenotype_observation_generated_temp
  set psg_e1a_name = (select term_name from term where psg_e1a_zdb_id = term_zdb_id);

update phenotype_observation_generated_temp
  set psg_e1b_name = (select term_name from term where psg_e1b_zdb_id = term_zdb_id);

update phenotype_observation_generated_temp
  set psg_e2a_name = (select term_name from term where psg_e2a_zdb_id = term_zdb_id);

update phenotype_observation_generated_temp
  set psg_e2b_name = (select term_name from term where psg_e2b_zdb_id = term_zdb_id);

update phenotype_observation_generated_temp
  set psg_quality_name = (select term_name from term where psg_quality_zdb_id = term_zdb_id);

update phenotype_observation_generated_temp
  set psg_e1a_zdb_id = null
  where psg_e1a_Zdb_id = '';

update phenotype_observation_generated_temp
  set psg_e1b_zdb_id = null
  where psg_e1b_Zdb_id = '';

update phenotype_observation_generated_temp
  set psg_e2a_zdb_id = null
  where psg_e2a_Zdb_id = '';

update phenotype_observation_generated_temp
  set psg_e2b_zdb_id = null
  where psg_e2b_Zdb_id = '';

update phenotype_observation_generated_temp
  set psg_e1_relation_name = 'occurs in'
  where exists (Select 'x' from term where term_zdb_id = psg_e2b_zdb_id
                and term_ontology = 'biological_process')
  and psg_e1b_zdb_id is not null;

update phenotype_observation_generated_temp
  set psg_e2_relation_name = 'occurs in'
  where exists (Select 'x' from term where term_zdb_id = psg_e2b_zdb_id
                and term_ontology = 'biological_process')
  and psg_e2b_zdb_id is not null;

-- morphological without normal tag
update phenotype_observation_generated_temp  
  set psg_short_name =  psg_e1a_name||nvl(" "||psg_e1b_name,'')||nvl(" "||psg_quality_name,'')||nvl(" "||psg_e2a_name,'')||nvl(" "||psg_e2b_name,'')||", "||psg_tag
  where psg_mrkr_zdb_id is null
  and psg_tag != 'normal';

-- morphological without normal tag
update phenotype_observation_generated_temp
  set psg_short_name =  psg_e1a_name||nvl(" "||psg_e1b_name,'')||nvl(" "||psg_quality_name,'')||nvl(" "||psg_e2a_name,'')||nvl(" "||psg_e2b_name,'')||", normal or recovered"
  where psg_mrkr_zdb_id is null
  and psg_tag = 'normal';

-- EaP with any tag
update phenotype_observation_generated_temp  
  set psg_short_name = psg_e1a_name||nvl(" "||psg_e1b_name,'')||" "||psg_mrkr_Abbrev||nvl(" "||psg_mrkr_relation,'')||nvl(" "||psg_quality_name,'')||", "||psg_tag
  where psg_mrkr_zdb_id is not null;

update statistics high for table phenotype_source_generated_temp;
update statistics high for table phenotype_observation_generated_temp;

insert into phenotype_generated_curated_mapping_temp (pgcm_pg_id, pgcm_source_id, pgcm_id_type)
  select distinct pg_id, xpatex_zdb_id, "expression"
    from phenotype_source_generated_temp, expression_figure_stage, expression_experiment2, expression_result2, expression_phenotype_term
    where pg_genox_zdb_id = xpatex_genox_Zdb_id
    and pg_fig_zdb_id = efs_fig_zdb_id
    and pg_start_stg_zdb_id = efs_start_stg_zdb_id
    and pg_end_stg_Zdb_id = efs_end_stg_zdb_id
    and xpatex_zdb_id = efs_xpatex_zdb_id
    and xpatres_efs_id = efs_pk_id
    and xpatres_pk_id = ept_xpatres_id;

insert into phenotype_generated_curated_mapping_temp (pgcm_pg_id, pgcm_source_id, pgcm_id_type)
  select distinct pg_id, phenox_pk_id, "phenotype"
    from phenotype_source_generated_temp, phenotype_experiment
    where pg_genox_zdb_id = phenox_genox_zdb_id
    and pg_fig_zdb_id = phenox_fig_zdb_id
    and pg_start_stg_zdb_id = phenox_start_stg_zdb_id
    and pg_end_stg_zdb_id = phenox_end_stg_zdb_id;
