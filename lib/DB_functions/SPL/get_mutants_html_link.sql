

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

	select count(distinct fmrel_ftr_zdb_id) :: integer 
	  into ftrCount
     	  from feature_marker_relationship, feature, feature_marker_relationship_type
         where fmrel_mrkr_zdb_id = geneZdbId
           and fmrel_ftr_zdb_id = feature_zdb_id
           and fmrel_type=fmreltype_name
           and fmreltype_produces_affected_marker='t';

	-- count in allele that knock out the gene

	let ftrCount = ftrCount; 

        select count(distinct genofeat_geno_zdb_id) :: integer 
	   into genoCount
           from feature_marker_relationship, genotype_feature
          where fmrel_mrkr_zdb_id = geneZdbId
            and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
            and fmrel_type in ('is allele of', 'markers missing', 'markers present');

	if ( genoCount == 1 ) then 
		select genofeat_geno_zdb_id
		  into genoZdbId
           	  from feature_marker_relationship, genotype_feature
          	 where fmrel_mrkr_zdb_id = geneZdbId
            	   and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
            	   and fmrel_type in ('is allele of', 'markers missing', 'markers present');
	end if 


	let genoCount = genoCount;

 
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
		return '<A HREF="//cgi-bin/webdriver?MIval=aa-genotypeview.apg&OID=' || genoZdbId ||'">' || genoCount ||' genotype' || genoSuffix|| '</A>';

        else
		return '<A HREF="//cgi-bin/webdriver?MIval=aa-fishselect.apg&query_results=exist&fsel_marker_id=' || geneZdbId ||'">' || genoCount ||' genotype' || genoSuffix || '</A>';

	end if ;

end function;
