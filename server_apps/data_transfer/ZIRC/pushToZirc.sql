-- use a tab as the delimiter in all these downloads


-- Generate wild type list

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinWildtype'
  delimiter '	'
  select geno_zdb_id, get_genotype_display(geno_zdb_id)
    from genotype
    where geno_is_wildtype = "t";

-- generate aliases for wild types

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinWildtypeAliases'
  delimiter '	'
  select dalias_data_zdb_id, dalias_alias
    from data_alias, genotype
    where dalias_data_zdb_id = geno_zdb_id
      and geno_is_wildtype = "t";


-- generate feature list

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinFeatures'
  delimiter '	'
  select feature_zdb_id as feature_id, 
	 feature_name as allele_feature_name, 
	 fmrel_mrkr_zdb_id as affected_gene,
	 geno_zdb_id as genotype,
         genofeat_chromosome as LG
    from feature, feature_marker_relationship, genotype_feature, genotype
    where feature_zdb_id = fmrel_ftr_zdb_id
    and genofeat_geno_zdb_id = geno_zdb_id
    and genofeat_feature_zdb_id = feature_zdb_id ;

-- generate aliases for features

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinFeatureAliases'
  delimiter '	'
  select dalias_data_zdb_id, dalias_alias
    from data_alias, feature
    where dalias_data_zdb_id = feature_zdb_id;

-- generate genotype alias

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinGenotypeAliases'
  delimiter '	'
  select dalias_data_zdb_id, dalias_alias
    from data_alias, genotype
    where dalias_data_zdb_id = geno_zdb_id;

-- generate locus list

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinLoci'
  delimiter '	'
  select distinct fmrel_mrkr_zdb_id, mrkr_name, mrkr_abbrev 
    from feature_marker_relationship, marker
    where fmrel_mrkr_zdb_id = mrkr_zdb_id;

-- generate aliases for locii

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinLocusAliases'
  delimiter '	'
  select distinct dalias_data_zdb_id, dalias_alias
    from data_alias, feature_marker_relationship, marker
    where dalias_data_zdb_id = mrkr_zdb_id
    and mrkr_zdb_id = fmrel_mrkr_zdb_id
    order by 1;

-- Generate EST list

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinEsts'
  delimiter '	'
  select mrkr_zdb_id, mrkr_name, mrkr_abbrev
    from marker
    where mrkr_type = "EST";

-- generate EST aliases

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinEstAliases'
  delimiter '	'
  select dalias_data_zdb_id, dalias_alias
    from data_alias, marker
    where dalias_data_zdb_id = mrkr_zdb_id
      and mrkr_type = "EST";
