-- use a tab as the delimiter in all these downloads


-- Generate wild type list

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinWildtypeLines'
  delimiter '	'
  select zdb_id, get_fish_full_name(zdb_id), name, abbrev
    from fish
    where line_type = "wild type";

-- generate aliases for wild types

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinWildtypeLineAliases'
  delimiter '	'
  select dalias_data_zdb_id, dalias_alias
    from data_alias, fish
    where dalias_data_zdb_id = fish.zdb_id
      and line_type = "wild type";


-- generate alteration / allele list

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinAlterations'
  delimiter '	'
  select zdb_id, allele, locus 
    from alteration;

-- generate aliases for alterations / alleles

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinAlterationAliases'
  delimiter '	'
  select dalias_data_zdb_id, dalias_alias
    from data_alias, alteration
    where dalias_data_zdb_id = alteration.zdb_id;


-- generate locus list

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinLoci'
  delimiter '	'
  select zdb_id, locus_name, abbrev 
    from locus;

-- generate aliases for locii

-- Currently, this gets aliases from locus_alias, but soon it will
-- get aliases from data_alias.  locus_alias is unnecessarily complicated.
-- Therefore so is this code.

unload to '<!--|ROOT_PATH|-->/home/data_transfer/ZIRC/zfinLocusAliases'
  delimiter '	'
  select lcsali_locus_zdb_id, lcsali_locus_name_alias
    from locus_alias
  union
  select lcsali_locus_zdb_id, lcsali_locus_abbrev_alias
    from locus_alias
    where lcsali_locus_abbrev_alias <> ""
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
