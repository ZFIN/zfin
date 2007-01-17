

create function get_mutants_html_link 
			(geneZdbId	like marker.mrkr_zdb_id)
	returning varchar(255);

  -- Create the genotype/allele link for a gene in a format of
  --     2 genotypes (2 alleles) 
  -- And it would be linked to a mutant search result page
  --  	 	
  -- 
  -- if the gene do not have mutants, return
  --   unknown
  -- otherwise, return
  --   # genotype (# alllele)  (hyperlinked)
  -- 

	define ftrCount	integer;
	define ftrCount_2	integer;
	define genoCount	integer;
	define genoCount_2	integer;
	define ftrSuffix	char;
	define genoSuffix	char;
	define genoZdbId	like genotype.geno_zdb_id;

        let ftrSuffix = '';
 	let genoSuffix = '';
	let genoZdbId = '';

	select count(fmrel_ftr_zdb_id) :: integer 
	  into ftrCount
     	  from feature_marker_relationship, feature
         where fmrel_mrkr_zdb_id = geneZdbId
           and fmrel_ftr_zdb_id = feature_zdb_id
           and fmrel_type = 'is allele of';

	-- count in allele that knock out the gene
	select count(allele) :: integer
	  into ftrCount_2
	  from mapped_deletion
	 where present_t = 'f'
           and marker_id = geneZdbId;

	let ftrCount = ftrCount + ftrCount_2;

        select count(genofeat_geno_zdb_id) :: integer 
	   into genoCount
           from feature_marker_relationship, genotype_feature
          where fmrel_mrkr_zdb_id = geneZdbId
            and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
            and fmrel_type = 'is allele of';

	if ( genoCount == 1 ) then 
		select genofeat_geno_zdb_id
		  into genoZdbId
           	  from feature_marker_relationship, genotype_feature
          	 where fmrel_mrkr_zdb_id = geneZdbId
            	   and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
            	   and fmrel_type = 'is allele of';
	end if 

	select count(genofeat_geno_zdb_id) :: integer 
	   into genoCount_2
           from mapped_deletion, feature, genotype_feature
          where present_t = 'f'
            and marker_id = geneZdbId
            and allele = feature_name
            and feature_zdb_id = genofeat_feature_zdb_id;

 	-- is_allele_of relationship and missing gene relation is
        -- mutually exclusive, so we won't be overwritten the value here.
	if ( genoCount_2 == 1 ) then 

	 	select genofeat_geno_zdb_id
	   	  into genoZdbId
          	  from mapped_deletion, feature, genotype_feature
          	 where present_t = 'f'
            	   and marker_id = geneZdbId
           	   and allele = feature_name
           	    and feature_zdb_id = genofeat_feature_zdb_id;
	end if

	let genoCount = genoCount + genoCount_2;

 
	if (ftrCount > 1) then
		let ftrSuffix = 's';
	end if;

	if (genoCount > 1) then
		let genoSuffix = 's';
	end if 

	if (ftrCount == 0) then 
		return '';

	elif (genoCount == 1 ) then
	-- in this case, the genoZdbId would either be filled with either of the two queris
        -- above but not both in which case genoCount would be 2.
		return '<A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-genotypeview.apg&OID=' || genoZdbId ||'">' || genoCount ||' genotype' || genoSuffix || ' (' || ftrCount || ' allele' || ftrSuffix || ')</A>';

        else
		return '<A HREF="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-fishselect.apg&query_results=exist&fsel_marker_id=' || geneZdbId ||'">' || genoCount ||' genotype' || genoSuffix || ' (' || ftrCount || ' allele' || ftrSuffix || ')</A>';

	end if ;

end function;
