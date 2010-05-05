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
  define genoBackground  lvarchar;
  define zygPrefix       like zygocity.zyg_gene_prefix;
  define zygAllele       lvarchar;
  define featSig         like feature_type.ftrtype_name;
  define startName       like genotype.geno_display_name;
  define wildtype        like genotype.geno_is_wildtype;
  
  select geno_display_name, geno_is_wildtype 
  into startName, wildtype 
  from genotype where geno_zdb_id = genoZdbId;

  if ( wildtype != 't') then  
  
    let genoDisplayHtml = '';
    let zygPrefix = '';
      
  
    foreach
       select distinct get_feature_abbrev_display(feature_zdb_id), 
              zyg_gene_prefix, 
              zyg_allele_display, 
              feature_abbrev,
              ftrtype_significance              
          into featAbbrevHtml, zygPrefix, zygAllele, featAbbrev, featSig
         from feature, genotype_feature, zygocity, feature_type
        where genofeat_geno_zdb_id = genoZdbId
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = zyg_zdb_id
          and feature_type = ftrtype_name
       order by 1

        if (featAbbrev like "unspecified%") then
          let featAbbrev = "unspecified";    
        end if
        if (featAbbrev like "unrec\_%") then
          let featAbbrev = "unrecovered";    
        end if
        
        if (zygAllele == '/allele') then
          let zygAllele = '/' || featAbbrev;
        end if
        
        if (zygPrefix is null) then
          let zygPrefix = '';
        end if
        
        if (zygAllele is null) then
          let zygAllele = '';
        end if
               
        if (genoDisplayHtml == '') then
          let genoDisplayHtml = zygPrefix || featAbbrevHtml ;
          
        else
            let genoDisplayHtml = genoDisplayHtml ||';'|| zygPrefix || featAbbrevHtml;
       
        end if


        if (zygAllele != '') then
        
          if (genoDisplayHtml like "%<sup>%") then
            let genoDisplayHtml = genoDisplayHtml || '<sup>' || zygAllele || '</sup>';
          else
            let genoDisplayHtml = genoDisplayHtml || zygAllele ;
          
          end if
        
        end if
        
    end foreach

    let genoDisplayHTML = replace(genoDisplayHTML,'</sup><sup>','');          

  else
  
    let genoDisplayHTML = startName;
  
  end if

  return genoDisplayHtml;

end function;
