-- use a tab as the delimiter in all these downloads


-- Generate wild type list

 create VIEW zfinWildType as
 select geno_zdb_id, get_genotype_display(geno_zdb_id)
    from genotype
    where geno_is_wildtype = 't';
    \copy (SELECT * FROM zfinWildType) to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinWildtype' with delimiter as '	';
DROP VIEW zfinWildType;

-- generate aliases for wild types

create VIEW zfinWildtypeAliases as
  select dalias_data_zdb_id, dalias_alias
    from data_alias, genotype
    where dalias_data_zdb_id = geno_zdb_id
      and geno_is_wildtype = "t";
      \copy (SELECT * FROM  zfinWildtypeAliases) to  '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinWildtypeAliases' with   delimiter as '	';
     DROP VIEW zfinWildtypeAliases;


-- generate feature list
create VIEW zfinFeatures as
  select feature_zdb_id as feature_id,
	 feature_name as allele_feature_name, 
	 fmrel_mrkr_zdb_id as affected_gene,
	 geno_zdb_id as genotype,
         lnkg_chromosome as LG
    from feature,linkage_member, linkage,
	feature_marker_relationship, genotype_feature, genotype
    where feature_zdb_id = fmrel_ftr_zdb_id
    and genofeat_geno_zdb_id = geno_zdb_id
    and genofeat_feature_zdb_id = feature_zdb_id 
    and lnkg_zdb_id = lnkgmem_linkage_zdb_id
    and lnkgmem_member_zdb_id = feature_zdb_id;
    \copy (SELECT * from zfinFeatures)  to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinFeatures' with   delimiter as  '	';
    DROP VIEW zfinFeatures;

-- generate aliases for features
create VIEW zfinFeatureAliases as
  select dalias_data_zdb_id, dalias_alias
    from data_alias, feature
    where dalias_data_zdb_id = feature_zdb_id;
\copy (SELECT * FROM zfinFeatureAliases) to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinFeatureAliases' with  delimiter as '	';
DROP VIEW zfinFeatureAliases;

-- generate genotype alias

create VIEW zfinGenotypeAliases as
  select dalias_data_zdb_id, dalias_alias
    from data_alias, genotype
    where dalias_data_zdb_id = geno_zdb_id;
    \copy (SELECT * FROM zfinGenotypeAliases) to  '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinGenotypeAliases' with  delimiter as  '	';
    DROP VIEW zfinGenotypeAliases;

-- generate locus list


create VIEW zfinLoci as
  select distinct fmrel_mrkr_zdb_id, mrkr_name, mrkr_abbrev 
    from feature_marker_relationship, marker
    where fmrel_mrkr_zdb_id = mrkr_zdb_id;
    \copy (SELECT * FROM  zfinLoci) to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinLoci' with   delimiter as  '	';
    DROP VIEW zfinLoci;

-- generate aliases for locii
create VIEW zfinLocusAliases as
  select distinct dalias_data_zdb_id, dalias_alias
    from data_alias, feature_marker_relationship, marker
    where dalias_data_zdb_id = mrkr_zdb_id
    and mrkr_zdb_id = fmrel_mrkr_zdb_id
    order by 1;
    \copy (SELECT * FROM  zfinLocusAliases) to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinLocusAliases' with  delimiter  as '	';
    DROP VIEW zfinLocusAliases;

-- Generate EST list
CREATE VIEW zfinEsts as
  select mrkr_zdb_id, mrkr_name, mrkr_abbrev
    from marker
    where mrkr_type = "EST";
    \copy (SELECT * FROM  zfinEsts) to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinLocusEsts' with  delimiter  as '	';
    DROP VIEW zfinEsts;

-- generate EST aliases
CREATE VIEW zfinEstAliases as
  select dalias_data_zdb_id, dalias_alias
    from data_alias, marker
    where dalias_data_zdb_id = mrkr_zdb_id
      and mrkr_type = "EST";
      \copy (SELECT * from zfinEstAliases) to  '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinEstAliases' with   delimiter as '	';
      DROP VIEW zfinEstAliases;
