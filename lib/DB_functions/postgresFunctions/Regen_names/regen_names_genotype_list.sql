create or replace function  regen_names_genotype_list()
returns void as $$

  -- ---------------------------------------------------------------------
  -- regen_names_genotype_list generates all names for the genotype identified by 
  -- the ZDB IDs in the regen_zdb_id_temp table and adds the names to the
  -- regen_all_names_temp table. 
  --
  -- We collect feature name, abbrev, alias, gene name, symbol and alias as well
  -- genotype alias. Name matching to genotypes are happening both at the current
  -- mutant search page and at the expression search for background. Though it is 
  -- duplicate to store the gene related names, we weight the search performance
  -- over the stretches of regen execution time and more records. 
  --
  -- PRECONDITIONS:
  --   regen_zdb_id_temp table exists and contains a list of genotype ZDB IDs
  --     to get names for.
  --   regen_all_names_temp table exists.  It may contain data, but it does
  --     not contain any data for feature.
  --
  -- INPUT VARS:
  --   none
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: Nothing
  --   Failure: Throws whatever exception happened.
  --
  -- EFFECTS:
  --   Success:
  --     regen_all_names_temp contains all names for genotype that were identified
  --       in regen_zdb_id_temp table.
  --   Error:
  --     regen_all_names_temp may or may not have new data in it.
  --     transaction is not committed or rolled back.
  -- ---------------------------------------------------------------------

  declare namePrecedence  name_precedence.nmprec_precedence%TYPE;
  	  nameSignificance  name_precedence.nmprec_significance%TYPE;


  begin

  ----------------------------------------
  -- Feature names/abbrev/alias
  ----------------------------------------

  namePrecedence = 'Genomic feature name';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;


  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select feature_name, rgnz_zdb_id, nameSignificance, namePrecedence, 
           lower(feature_name)
      from feature, regen_zdb_id_temp, genotype_feature
     where genofeat_geno_zdb_id = rgnz_zdb_id
       and genofeat_feature_zdb_id = feature_zdb_id;


  namePrecedence = 'Genomic feature abbreviation';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select feature_abbrev, rgnz_zdb_id, nameSignificance, namePrecedence, 
           lower(feature_abbrev)
      from feature, regen_zdb_id_temp, genotype_feature
     where genofeat_geno_zdb_id = rgnz_zdb_id
       and genofeat_feature_zdb_id = feature_zdb_id;

  -- get aliases
  -- note: we use "select distinct" to eliminates
  -- the odds that different features of the same genotype
  -- has the same previous names

  namePrecedence = 'Genomic feature alias';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select distinct dalias_alias, rgnz_zdb_id, nameSignificance, namePrecedence, 
           dalias_alias_lower
      from data_alias, regen_zdb_id_temp, genotype_feature
     where genofeat_geno_zdb_id = rgnz_zdb_id
       and genofeat_feature_zdb_id = dalias_data_zdb_id;

  

  ----------------------------------------
  -- GENE 
  ----------------------------------------

  insert into regen_geno_related_gene_zdb_id_temp
       select fmrel_mrkr_zdb_id, rgnz_zdb_id
         from regen_zdb_id_temp, feature_marker_relationship, genotype_feature, feature_marker_relationship_type
        where genofeat_geno_zdb_id = rgnz_zdb_id
          and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
          and fmrel_type=fmreltype_name
          and fmreltype_produces_affected_marker='t'
          and fmrel_type = "is allele of";

  insert into regen_geno_related_gene_zdb_id_temp
       select mrel_mrkr_2_zdb_id, rgnz_zdb_id
         from regen_zdb_id_temp, feature_marker_relationship, genotype_feature,
              marker_relationship
        where genofeat_geno_zdb_id = rgnz_zdb_id
          and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
          and fmrel_mrkr_zdb_id = mrel_mrkr_1_zdb_id
          and mrel_type in ("promoter of", "coding sequence of");

  insert into regen_geno_related_gene_zdb_id_distinct_temp
       select distinct rgnrgz_gene_zdb_id, rgnrgz_geno_zdb_id
         from regen_geno_related_gene_zdb_id_temp;


    ----------------------------------------
    -- Gene names/abbrev/alias
    ----------------------------------------

  namePrecedence = 'Gene symbol';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select mrkr_abbrev, rgnz_zdb_id, nameSignificance, namePrecedence, 
           lower(mrkr_abbrev)
      from marker, regen_zdb_id_temp, regen_geno_related_gene_zdb_id_distinct_temp
      where rgnrgzd_geno_zdb_id = rgnz_zdb_id
        and rgnrgzd_gene_zdb_id= mrkr_zdb_id;


  namePrecedence = 'Gene name';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select mrkr_name, rgnz_zdb_id, nameSignificance, namePrecedence, 
           lower(mrkr_name)
      from marker, regen_zdb_id_temp, regen_geno_related_gene_zdb_id_distinct_temp
      where rgnrgzd_geno_zdb_id = rgnz_zdb_id
        and rgnrgzd_gene_zdb_id= mrkr_zdb_id;


  namePrecedence = 'Gene alias';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select distinct dalias_alias, rgnz_zdb_id, nameSignificance, namePrecedence, 
           lower(dalias_alias)
      from data_alias, regen_zdb_id_temp, regen_geno_related_gene_zdb_id_distinct_temp
      where rgnrgzd_geno_zdb_id = rgnz_zdb_id
        and rgnrgzd_gene_zdb_id = dalias_data_zdb_id;


  ----------------------------------------
  -- Wildtype name
  ----------------------------------------

  namePrecedence = 'Wildtype name';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
	select geno_display_name, geno_zdb_id, nameSignificance,
	       namePrecedence, lower(geno_display_name)
          from genotype, regen_zdb_id_temp
         where geno_is_wildtype = "t"
           and geno_zdb_id = rgnz_zdb_id;

 insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
	select geno_handle, geno_zdb_id, nameSignificance,
	       namePrecedence, lower(geno_handle)
          from genotype, regen_zdb_id_temp
         where geno_is_wildtype = "t"
           and geno_zdb_id = rgnz_zdb_id
           and geno_display_name <> geno_handle;



  ----------------------------------------
  -- Genotype alias
  ----------------------------------------

  namePrecedence = 'Genotype alias';
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select dalias_alias, rgnz_zdb_id, nameSignificance, namePrecedence, 
           dalias_alias_lower
      from data_alias, regen_zdb_id_temp
     where dalias_data_zdb_id = rgnz_zdb_id;


  -- remove less significant dups

  perform regen_names_drop_dups();

  -- generate all_name_ends.  Takes regen_zdb_id_temp, regen_all_names_temp
  -- as input, adds recs to regen_all_name_ends_temp

  perform regen_name_ends_list();

 end;


$$ LANGUAGE plpgsql;
