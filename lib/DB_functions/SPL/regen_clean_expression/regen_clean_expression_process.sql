create procedure regen_clean_expression_process()

  -- --------------------------------------------------------------------------------------------
  -- Generates marker_zdb_id genox_zdb_id pairs for records in clean_expression_fast_search table 
  --
  -- PRECONDITONS:
  --   regen_genox_input_zdb_id_temp table exists and contains a list of gene and/or MO ZDB IDs
  --     to generate marker_zdb_id genox_zdb_id pairs for records in clean_expression_fast_search table
  --   regen_genox_temp table exists
  --
  -- INPUT VARS:
  --   none
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: nothing
  --   Failure: throws whatever exception happened.
  -- 
  -- EFFECTS:
  --   Success:
  --     regen_genox_temp contains marker_zdb_id genox_zdb_id pairs
  --   Error:
  --     regen_genox_temp may or may not have had data added to it.
  --     transaction is not committed or rolled back.
  -- --------------------------------------------------------------------------------------------

--  define markerZdbId like zdb_active_data.zactvd_zdb_id;

  begin
    on exception in (-958, -316)
      -- Ignore these errors:
      --  -958: Temp table already exists.
      --  -316: Index name already exists.
    end exception with resume;

  end

--  foreach
--    select rggz_zdb_id
--      into markerZdbId
--      from regen_genox_input_zdb_id_temp

   --------------
   -- for gene
   --------------


-- expression genes in WT genos in std/generic control environments
insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct rggz_zdb_id, genox_zdb_id
    from expression_experiment, fish_experiment, expression_Result, expression_pattern_figure, genotype, regen_genox_input_zdb_id_temp, fish
    where genox_zdb_id = xpatex_genox_zdb_id
     and xpatex_zdb_id = xpatres_xpatex_zdb_id
     and xpatres_zdb_id = xpatfig_xpatres_zdb_id
    and genox_is_std_or_generic_control = 't'
    and genox_fish_zdb_id = fish_Zdb_id
    and fish_genotype_zdb_id = geno_zdb_id
    and geno_is_wildtype = 't'
   and rggz_zdb_id [1,8] = "ZDB-GENE"
 and rggz_zdb_id = xpatex_gene_zdb_id;


-- The genotypes include those innocuous transgenics only, and the environment is 'Standard' or 'Generic Control

insert into regen_genox_temp(rggt_mrkr_zdb_id, rggt_genox_zdb_id)
select distinct xpatex_gene_zdb_id, genox_zdb_id
    from feature_marker_relationship, genotype_feature, fish_experiment, expression_experiment, expression_result, expression_pattern_figure, regen_genox_input_zdb_id_temp, fish
   where xpatex_gene_zdb_id[1,8] = "ZDB-GENE"
    and rggz_zdb_id = xpatex_gene_zdb_id
     and genox_zdb_id = xpatex_genox_Zdb_id
     and genox_is_std_or_generic_control = 't'
     and xpatex_zdb_id = xpatres_xpatex_zdb_id
     and xpatres_zdb_id = xpatfig_xpatres_zdb_id
     and fmrel_type = "contains innocuous sequence feature"
     and not exists (select 'x'
                       from feature_marker_relationship Phenotypic, genotype_feature AnotherFeature
                      where genox_geno_zdb_id = AnotherFeature.genofeat_geno_zdb_id
                       and AnotherFeature.genofeat_feature_zdb_id = Phenotypic.fmrel_ftr_zdb_id
                        and Phenotypic.fmrel_type != 'contains innocuous sequence feature')      
     and not exists (select 'x'
                      from genotype_feature OtherPhenotypicFeature, feature
                      where genox_geno_zdb_id = OtherPhenotypicFeature.genofeat_geno_zdb_id
                        and OtherPhenotypicFeature.genofeat_feature_zdb_id = feature_zdb_id
                        and feature_type in ('DEFICIENCY','TRANSLOC','COMPLEX_SUBSTITUTION','INVERSION','POINT_MUTATION','DELETION','SEQUENCE_VARIANT','UNSPECIFIED','INDEL'))
     and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
     and genox_fish_zdb_id = fish_zdb_id
     and fish_genotype_zdb_id = genofeat_geno_zdb_id;


                          
       
--    end if

--  end foreach  -- foreach record in regen_genox_input_zdb_id_temp

end procedure;
