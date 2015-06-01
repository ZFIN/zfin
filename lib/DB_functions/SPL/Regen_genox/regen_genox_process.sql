create procedure regen_genox_process()

  -- --------------------------------------------------------------------------------------------
  -- Generates marker_zdb_id genox_zdb_id pairs for records in mutant_fast_search table 
  --
  -- PRECONDITONS:
  --   regen_genox_input_zdb_id_temp table exists and contains a list of gene and/or MO ZDB IDs
  --     to generate marker_zdb_id genox_zdb_id pairs for records in mutant_fast_search table
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


-- The genotypes include any of the WT lines, which (cannot be with 'Standard' or 'Generic Control' environments),
-- and has MO(s) which target ONLY this gene
insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct rggz_zdb_id, genox_zdb_id
    from marker_relationship, fish_experiment, genotype, regen_genox_input_zdb_id_temp, fish_str, experiment, experiment_Condition, fish
   where mrel_mrkr_2_zdb_id = rggz_zdb_id
     and rggz_zdb_id [1,8] = "ZDB-GENE"
     and mrel_type = 'knockdown reagent targets gene'
     and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
     and not exists(select NotThisMO.expcond_mrkr_zdb_id
                      from marker_relationship NotThisMrkr, fish_str NotThisMO
                     where NotThisMO.fishstr_str_zdb_id = NotThisMrkr.mrel_mrkr_1_zdb_id 
                       and NotThisMrkr.mrel_type = 'knockdown reagent targets gene'
                       and NotThisMrkr.mrel_mrkr_2_zdb_id != rggz_zdb_id) 
     and not exists(select NOTstr.fishstr_str_zdb_id 
                      from fish_str NOTstr
                     where NOTstr.fishstr_str_zdb_id is null)                       
     and expcond_exp_zdb_id = exp_zdb_id
     and exp_name not like '\_%'
     and genox_exp_zdb_id = exp_zdb_id
     and genox_fish_zdb_id = fish_zdb_id
     and fish_genotype_zdb_id = geno_zdb_id
     and geno_is_wildtype = 't'
     and exists (select phenox_genox_zdb_id
                   from phenotype_experiment, phenotype_statement 
                  where phenox_genox_zdb_id = genox_zdb_id  
                    and phenox_pk_id = phenos_phenox_pk_id
                    and phenos_tag != 'normal') ;

-- The genotypes include those with alleles of the gene, which cannot be allele of other gene
-- and cannot have phenotypic Tg, and the environment is 'Standard' or 'Generic Control'
insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct rggz_zdb_id, genox_zdb_id
    from feature_marker_relationship, genotype_feature, genotype_experiment, regen_genox_input_zdb_id_temp
   where fmrel_mrkr_zdb_id = rggz_zdb_id  
     and rggz_zdb_id [1,8] = "ZDB-GENE"
     and fmrel_type = "is allele of"
     and not exists (select OtherMrkr.fmrel_ftr_zdb_id
                       from feature_marker_relationship OtherMrkr, genotype_feature OtherFeature
                      where genox_geno_zdb_id = OtherFeature.genofeat_geno_zdb_id
                        and OtherFeature.genofeat_feature_zdb_id = OtherMrkr.fmrel_ftr_zdb_id
                        and OtherMrkr.fmrel_type = 'is allele of'
                        and OtherMrkr.fmrel_mrkr_zdb_id != rggz_zdb_id)  
     and not exists (select 'x'
                       from feature_marker_relationship Phenotypic, genotype_feature AnotherFeature
                      where genox_geno_zdb_id = AnotherFeature.genofeat_geno_zdb_id
                        and AnotherFeature.genofeat_feature_zdb_id = Phenotypic.fmrel_ftr_zdb_id
                        and Phenotypic.fmrel_type = 'contains phenotypic sequence feature') 
     and not exists (select 'x'
                       from genotype_feature OtherPhenotypicFeature, feature
                      where genox_geno_zdb_id = OtherPhenotypicFeature.genofeat_geno_zdb_id
                        and OtherPhenotypicFeature.genofeat_feature_zdb_id = feature_zdb_id
                        and feature_type in ('DEFICIENCY','TRANSLOC','COMPLEX_SUBSTITUTION','INVERSION')) 
     and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
     and genox_geno_zdb_id = genofeat_geno_zdb_id                   
     and not exists (select exp_name 
                       from experiment Generic
                      where Generic.exp_zdb_id = genox_exp_zdb_id
                        and Generic.exp_name not like "\_%") 
     and exists (select phenox_genox_zdb_id
                   from phenotype_experiment, phenotype_statement 
                  where phenox_genox_zdb_id = genox_zdb_id  
                    and phenox_pk_id = phenos_phenox_pk_id
                    and phenos_tag != 'normal') ;
                    

-- The genotypes include those with alleles of the gene, which cannot be allele of other gene and
-- cannot involve any phenotypic Tg, and have MO(s) which target ONLY this gene                    
insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct rggz_zdb_id, genox_zdb_id
    from feature_marker_relationship, genotype_feature, genotype_experiment, experiment_condition, marker_relationship, regen_genox_input_zdb_id_temp
   where fmrel_mrkr_zdb_id = rggz_zdb_id  
     and rggz_zdb_id [1,8] = "ZDB-GENE"
     and fmrel_type = "is allele of"
     and not exists (select OtherMrkr.fmrel_ftr_zdb_id
                       from feature_marker_relationship OtherMrkr, genotype_feature OtherFeature
                      where genox_geno_zdb_id = OtherFeature.genofeat_geno_zdb_id
                        and OtherFeature.genofeat_feature_zdb_id = OtherMrkr.fmrel_ftr_zdb_id
                        and OtherMrkr.fmrel_type = 'is allele of'
                        and OtherMrkr.fmrel_mrkr_zdb_id != rggz_zdb_id)   
     and not exists (select 'x'
                       from feature_marker_relationship Phenotypic, genotype_feature AnotherFeature
                      where genox_geno_zdb_id = AnotherFeature.genofeat_geno_zdb_id
                        and AnotherFeature.genofeat_feature_zdb_id = Phenotypic.fmrel_ftr_zdb_id
                        and Phenotypic.fmrel_type = 'contains phenotypic sequence feature')                         
     and not exists (select 'x'
                       from genotype_feature OtherPhenotypicFeature, feature
                      where genox_geno_zdb_id = OtherPhenotypicFeature.genofeat_geno_zdb_id
                        and OtherPhenotypicFeature.genofeat_feature_zdb_id = feature_zdb_id
                        and feature_type in ('DEFICIENCY','TRANSLOC','COMPLEX_SUBSTITUTION','INVERSION')) 
     and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
     and genox_geno_zdb_id = genofeat_geno_zdb_id   
     and expcond_exp_zdb_id = genox_exp_zdb_id 
     and expcond_mrkr_zdb_id = mrel_mrkr_1_zdb_id 
     and mrel_type = 'knockdown reagent targets gene'
     and mrel_mrkr_2_zdb_id = rggz_zdb_id
     and not exists(select NotThisMO.expcond_mrkr_zdb_id
                      from marker_relationship NotThisMrkr, experiment_condition NotThisMO
                     where NotThisMO.expcond_exp_zdb_id = genox_exp_zdb_id 
                       and NotThisMO.expcond_mrkr_zdb_id = NotThisMrkr.mrel_mrkr_1_zdb_id 
                       and NotThisMrkr.mrel_type = 'knockdown reagent targets gene'
                       and NotThisMrkr.mrel_mrkr_2_zdb_id != rggz_zdb_id)  
     and not exists(select NOTstr.expcond_mrkr_zdb_id 
                      from experiment_condition NOTstr
                     where NOTstr.expcond_exp_zdb_id = genox_exp_zdb_id 
                       and NOTstr.expcond_mrkr_zdb_id is null)                       
     and exists (select phenox_genox_zdb_id
                   from phenotype_experiment, phenotype_statement 
                  where phenox_genox_zdb_id = genox_zdb_id  
                    and phenox_pk_id = phenos_phenox_pk_id
                    and phenos_tag != 'normal') ;
                    
-- The genotypes include those innocuous transgenics only, and the environment is 'Standard' or 'Generic Control'
insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct rggz_zdb_id, genox_zdb_id
    from feature_marker_relationship, genotype_feature, genotype_experiment, regen_genox_input_zdb_id_temp
   where fmrel_mrkr_zdb_id = rggz_zdb_id  
     and rggz_zdb_id [1,8] = "ZDB-GENE"
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
                        and feature_type in ('DEFICIENCY','TRANSLOC','COMPLEX_SUBSTITUTION','INVERSION'))
     and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
     and genox_geno_zdb_id = genofeat_geno_zdb_id
     and not exists (select exp_name 
                       from experiment Generic
                      where Generic.exp_zdb_id = genox_exp_zdb_id
                        and Generic.exp_name not like "\_%") 
     and exists (select phenox_genox_zdb_id
                   from phenotype_experiment, phenotype_statement 
                  where phenox_genox_zdb_id = genox_zdb_id  
                    and phenox_pk_id = phenos_phenox_pk_id
                    and phenos_tag != 'normal') ;

-- The genotypes include those innocuous transgenics only, but no phenotypic Tg or allele to other gene,
-- and have MO(s) which target ONLY this gene
insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct rggz_zdb_id, genox_zdb_id
    from feature_marker_relationship, genotype_feature, genotype_experiment, experiment_condition, marker_relationship, regen_genox_input_zdb_id_temp
   where mrel_mrkr_2_zdb_id = rggz_zdb_id  
     and rggz_zdb_id [1,8] = "ZDB-GENE"
     and mrel_type = 'knockdown reagent targets gene'
     and mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id
     and not exists(select NotThisMO.expcond_mrkr_zdb_id
                      from marker_relationship NotThisMrkr, experiment_condition NotThisMO
                     where NotThisMO.expcond_exp_zdb_id = genox_exp_zdb_id 
                       and NotThisMO.expcond_mrkr_zdb_id = NotThisMrkr.mrel_mrkr_1_zdb_id 
                       and NotThisMrkr.mrel_type = 'knockdown reagent targets gene'
                       and NotThisMrkr.mrel_mrkr_2_zdb_id != rggz_zdb_id) 
     and not exists(select NOTstr.expcond_mrkr_zdb_id 
                      from experiment_condition NOTstr
                     where NOTstr.expcond_exp_zdb_id = genox_exp_zdb_id 
                       and NOTstr.expcond_mrkr_zdb_id is null)                       
     and expcond_mrkr_zdb_id = mrel_mrkr_1_zdb_id
     and expcond_exp_zdb_id = genox_exp_zdb_id 
     and genox_geno_zdb_id = genofeat_geno_zdb_id
     and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
     and fmrel_type = "contains innocuous sequence feature"
     and not exists (select 'x'
                       from feature_marker_relationship Phenotypic, genotype_feature AnotherFeature
                      where genox_geno_zdb_id = AnotherFeature.genofeat_geno_zdb_id
                        and AnotherFeature.genofeat_feature_zdb_id = Phenotypic.fmrel_ftr_zdb_id
                        and Phenotypic.fmrel_type = 'contains phenotypic sequence feature')   
     and not exists (select 'x'
                       from feature_marker_relationship AlleleToOtherGene, genotype_feature OtherFeature
                      where genox_geno_zdb_id = OtherFeature.genofeat_geno_zdb_id
                        and OtherFeature.genofeat_feature_zdb_id = AlleleToOtherGene.fmrel_ftr_zdb_id
                        and AlleleToOtherGene.fmrel_type = 'is allele of'
                        and AlleleToOtherGene.fmrel_mrkr_zdb_id != rggz_zdb_id)                          
     and not exists (select 'x'
                       from genotype_feature OtherPhenotypicFeature, feature
                      where genox_geno_zdb_id = OtherPhenotypicFeature.genofeat_geno_zdb_id
                        and OtherPhenotypicFeature.genofeat_feature_zdb_id = feature_zdb_id
                        and feature_type in ('DEFICIENCY','TRANSLOC','COMPLEX_SUBSTITUTION','INVERSION'))
     and exists (select phenox_genox_zdb_id
                   from phenotype_experiment, phenotype_statement 
                  where phenox_genox_zdb_id = genox_zdb_id  
                    and phenox_pk_id = phenos_phenox_pk_id
                    and phenos_tag != 'normal') ;

   ------------------------------
   -- for MO, TALEN and CRISPR
   ------------------------------
    
      -- The genotypes include any of the WT lines, and no other MO, TALEN or CRISPR involved
      insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
        select distinct rggz_zdb_id, genox_zdb_id
          from genotype_experiment, experiment_condition, regen_genox_input_zdb_id_temp
         where expcond_mrkr_zdb_id = rggz_zdb_id
	   and rggz_zdb_id [1,8] != "ZDB-GENE"
           and expcond_exp_zdb_id = genox_exp_zdb_id
           and exists (select 'x'
                         from genotype WT
                        where genox_geno_zdb_id = WT.geno_zdb_id
                          and WT.geno_is_wildtype = "t")
           and not exists(select 'x' 
                            from experiment_condition NOTstr
                           where NOTstr.expcond_exp_zdb_id = genox_exp_zdb_id 
                             and NOTstr.expcond_mrkr_zdb_id != rggz_zdb_id)
           and not exists(select 'x' 
                            from experiment_condition NOTstr
                           where NOTstr.expcond_exp_zdb_id = genox_exp_zdb_id 
                             and NOTstr.expcond_mrkr_zdb_id is null)
           and not exists(select 'x' 
                            from feature_marker_relationship NOTmrkr,genotype_feature NOTfeat,marker_relationship NOTstr
                           where genox_geno_zdb_id = NOTfeat.genofeat_geno_zdb_id
                             and NOTfeat.genofeat_feature_zdb_id = NOTmrkr.fmrel_ftr_zdb_id
                             and NOTmrkr.fmrel_mrkr_zdb_id = NOTstr.mrel_mrkr_1_zdb_id 
                             and NOTstr.mrel_mrkr_2_zdb_id != rggz_zdb_id
                             and NOTmrkr.fmrel_type = "is allele of") 
           and exists (select phenox_genox_zdb_id
                         from phenotype_experiment, phenotype_statement 
                        where phenox_genox_zdb_id = genox_zdb_id  
                          and phenox_pk_id = phenos_phenox_pk_id
                          and phenos_tag != 'normal') ;

      -- The genotypes include none phenotypic Tg feature only, which are NOT alleles of any gene, and no other MO, TALEN or CRISPR involved
      insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
        select distinct rggz_zdb_id, genox_zdb_id
          from genotype_feature, feature_marker_relationship, genotype_experiment, experiment_condition, regen_genox_input_zdb_id_temp
         where expcond_mrkr_zdb_id = rggz_zdb_id
	   and rggz_zdb_id [1,8] != "ZDB-GENE"
           and expcond_exp_zdb_id = genox_exp_zdb_id
           and genox_geno_zdb_id = genofeat_geno_zdb_id
           and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
           and fmrel_type = "contains innocuous sequence feature"
           and not exists (select 'x'
                             from feature_marker_relationship Phenotypic, genotype_feature AnotherFeature
                            where genox_geno_zdb_id = AnotherFeature.genofeat_geno_zdb_id
                              and AnotherFeature.genofeat_feature_zdb_id = Phenotypic.fmrel_ftr_zdb_id
                              and Phenotypic.fmrel_type = 'contains phenotypic sequence feature')  
           and not exists (select 'x'
                             from genotype_feature OtherPhenotypicFeature, feature
                            where genox_geno_zdb_id = OtherPhenotypicFeature.genofeat_geno_zdb_id
                              and OtherPhenotypicFeature.genofeat_feature_zdb_id = feature_zdb_id
                              and feature_type != 'TRANSGENIC_INSERTION')                              
           and not exists(select 'x' 
                            from experiment_condition NOTstr
                           where NOTstr.expcond_exp_zdb_id = genox_exp_zdb_id 
                             and NOTstr.expcond_mrkr_zdb_id != rggz_zdb_id)
           and not exists(select 'x'
                            from experiment_condition NOTstr
                           where NOTstr.expcond_exp_zdb_id = genox_exp_zdb_id 
                             and NOTstr.expcond_mrkr_zdb_id is null)
           and not exists(select 'x' 
                            from feature_marker_relationship NOTmrkr,genotype_feature NOTfeat,marker_relationship NOTstr
                           where genox_geno_zdb_id = NOTfeat.genofeat_geno_zdb_id
                             and NOTfeat.genofeat_feature_zdb_id = NOTmrkr.fmrel_ftr_zdb_id
                             and NOTmrkr.fmrel_mrkr_zdb_id = NOTstr.mrel_mrkr_1_zdb_id 
                             and NOTstr.mrel_mrkr_2_zdb_id != rggz_zdb_id
                             and NOTmrkr.fmrel_type = "is allele of") 
           and exists (select phenox_genox_zdb_id
                         from phenotype_experiment, phenotype_statement 
                        where phenox_genox_zdb_id = genox_zdb_id  
                          and phenox_pk_id = phenos_phenox_pk_id
                          and phenos_tag != 'normal') ;
                          
       
--    end if

--  end foreach  -- foreach record in regen_genox_input_zdb_id_temp

end procedure;
