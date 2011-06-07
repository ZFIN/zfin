create function get_genotype_display( genoZdbId varchar(50) )

  returning lvarchar;	

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a genotype, returns the genotype display name in 
  -- html format. 
  -- [the same value that is stored in the genotype table. this function
  --  generates the value that is stored.]
  --
  --  INPUT VARS: 
  --     genoZdbId   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     genotype display with proper HTML formatting. 
  --     NULL if genoZdbId does not exist in genotype table.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  define genoDisplayHtml lvarchar;
  define featAbbrev	 like feature.feature_abbrev;
 
  define featAbbrevHtml  lvarchar;
  define fad2		 lvarchar;
  define genoBackground  lvarchar;
  define zygAllele       lvarchar;

  define featSig         like feature_type.ftrtype_significance;
  define startName       like genotype.geno_display_name;
  define wildtype        like genotype.geno_is_wildtype;
  define featType	 like feature.feature_type;
  define zygOrder	 like zygocity.zyg_abbrev;
  define mrkrAbbrev	 like marker.mrkr_abbrev;
 

  select geno_display_name, geno_is_wildtype 
  into startName, wildtype 
  from genotype where geno_zdb_id = genoZdbId;

  let fad2 = '';

  if ( wildtype != 't') then  
  
    let genoDisplayHtml = '';
  
 
      ---add numbers to TG insertion significance in this routine.
--ZDB-GENO-100601-2
--ZDB-GENO-080905-5
--ZDB-GENO-080418-2 
--ZDB-GENO-080905-6 
--ZDB-GENO-090603-10
--ZDB-GENO-100215-6
--ZDB-GENO-100325-5

    foreach
       select distinct get_feature_abbrev_display(feature_zdb_id) as fad,  
              zyg_allele_display, 
	      case
		when mrkr_abbrev is null 
		then lower(get_feature_abbrev_display(feature_zdb_id))
		else lower(mrkr_abbrev)||get_feature_abbrev_display(feature_zdb_id)
                end as fad2,
	      feature_Abbrev,
	      feature_type,
	      case 
	      	   when feature_type = 'TRANSGENIC_INSERTION'
		   then ftrtype_significance +2
		   when feature_type = 'UNSPECIFIED'
		   then ftrtype_significance -2
		   else ftrtype_significance
		   end,
	     zyg_abbrev
         from feature, genotype_feature, zygocity, feature_type,  outer (feature_marker_relationship, outer marker)
        where genofeat_geno_zdb_id = genoZdbId
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = zyg_zdb_id
	  and feature_type = ftrtype_name
	  and fmrel_mrkr_zdb_id = mrkr_zdb_id
	  and fmrel_ftr_zdb_id = feature_zdb_id
	  and fmrel_type = 'is allele of'
	  union 
	select distinct get_feature_abbrev_display(feature_zdb_id) as fad, 
                            zyg_allele_display, 
              lower(get_feature_abbrev_display(feature_zdb_id)) as fad2,
	      feature_Abbrev,
	      feature_type,
	      case 
	      	   when feature_type = 'TRANSGENIC_INSERTION'
		   then ftrtype_significance +2
	   when feature_type = 'UNSPECIFIED'
		   then ftrtype_significance -2
		   else ftrtype_significance
		   end,
		   zyg_abbrev
          into featAbbrevHtml, zygAllele, mrkrAbbrev,featAbbrev, featType, featSig, zygOrder
         from feature, genotype_feature, zygocity, feature_type,
	      feature_marker_relationship
        where genofeat_geno_zdb_id = genoZdbId
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = zyg_zdb_id
	  and feature_type = ftrtype_name
	  and fmrel_ftr_zdb_id = feature_zdb_id
	  and fmrel_type not in ('is allele of')
       order by zyg_abbrev  , fad2, fad asc
       

  if (fad2 == featAbbrevHtml) then
	       continue foreach;
       else
     
            if (featAbbrev like "%unspecified" and featType != 'TRANSGENIC_UNSPECIFIED') then
               let featAbbrev = "unspecified";    
            end if
       	    if (featAbbrev like "%unrecovered") then
               let featAbbrev = "unrecovered";    
            end if
        
	    if (zygAllele == '/allele' and featType = 'TRANSGENIC_UNSPECIFIED') then
               let zygAllele = '/' || "unspecified";
	    elif (zygAllele == '/allele' and featType != 'TRANSGENIC_UNSPECIFIED') then
               let zygAllele = '/' || featAbbrev;
	    else
	    end if
        
          
	    if (zygAllele is null) then
               let zygAllele = '';
            end if
               
            if (genoDisplayHtml == '') then
               let genoDisplayHtml =  featAbbrevHtml ;
          
	    else
		   let genoDisplayHtml = genoDisplayHtml ||';'||  featAbbrevHtml;
       
            end if


            if (zygAllele != '') then
        
		if (featAbbrevHtml like "%<sup>%") then
            	   let genoDisplayHtml = genoDisplayHtml || '<sup>' || zygAllele || '</sup>';
            else
		   let genoDisplayHtml = genoDisplayHtml || zygAllele ;
	    end if
               

           end if

	   let fad2 = featAbbrevHtml ;
	   end if ;

   end foreach

    let genoDisplayHTML = replace(genoDisplayHTML,'</sup><sup>','');          

  else
  
    let genoDisplayHTML = startName;
  
  end if

  return genoDisplayHtml;

end function;