begin work ;

create function get_feature_abbrev_display( featZdbId varchar(50) )

  returning lvarchar;	

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a feature, returns the feature abbrev as a
  -- supper script of the gene.
  --
  --  INPUT VARS: 
  --     featZdbId   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     feature abbrev with proper HTML formatting. 
  --     NULL if featZdbId does not exist in feature table.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  define featAbbrevHtml lvarchar;
  define featAbbrev	like feature.feature_abbrev;
  define featName	like feature.feature_name;
  define featMrkrAbbrev like marker.mrkr_abbrev;
  
  let featMrkrAbbrev = "default";

  select feature_abbrev, mrkr_abbrev, feature_name
    into featAbbrev, featMrkrAbbrev, featName
    from feature, outer(feature_marker_relationship, marker)
   where feature_zdb_id = featZdbId
     and fmrel_ftr_zdb_id = feature_zdb_id
     and fmrel_mrkr_zdb_id = mrkr_zdb_id;

  if (featAbbrev is null) then
    let featAbbrevHtml = null;
  else
  
    if (featAbbrev like "%un\_%") then
      let featAbbrev = "unspecified";    
    end if
    
    let featAbbrevHtml = 
	featMrkrAbbrev || '<sup>' || featAbbrev || '</sup>';

    if (featMrkrAbbrev is null) then
       let featAbbrevHtml = featName;    
    end if

  end if  -- feat exists

  return featAbbrevHtml;

end function;

--==========================================================================

create function get_feature_abbrev_html( featZdbId varchar(50) )

  returning lvarchar;	

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a feature, returns the feature abbrev properly 
  -- html formatted
  --
  --  INPUT VARS: 
  --     featZdbId   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     feature abbrev with proper HTML formatting. 
  --     NULL if featZdbId does not exist in feature table.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  define featAbbrevHtml lvarchar;
  define featAbbrev	like feature.feature_abbrev;
  define featMrkrAbbrev like marker.mrkr_abbrev;

  select feature_abbrev, mrkr_abbrev
    into featAbbrev, featMrkrAbbrev
    from feature, feature_marker_relationship, marker
   where feature_zdb_id = featZdbId
     and fmrel_ftr_zdb_id = feature_zdb_id
     and fmrel_mrkr_zdb_id = mrkr_zdb_id;

  if (featAbbrev is null) then
    let featAbbrevHtml = null;
  else
  
    if (featAbbrev like "%un\_%") then
      let featAbbrev = REPLACE(featAbbrev, featMrkrAbbrev, "unspecified");    
    end if
    
    let featAbbrevHtml = 
	'<span class="mutant" title="'|| featAbbrev || '">' || featAbbrev || '</span>';

  end if  -- feat exists

  return featAbbrevHtml;

end function;

--============================================================================

create function get_geno_name_html_link( genoZdbId varchar(50) )
 
  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a genotype, this returns the genotype name
  -- properly formatted and embedded in an HTML link to the geno view page.
  --
  -- INPUT VARS: 
  --   genoZdbId   
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   genotype name with proper HTML formatting, embedded in an HTML link
  --   NULL if genoZdbId does not exist in genotype table
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  define genoName	like genotype.geno_display_name;
  define genoNameHTML	lvarchar;
  define genoIsWildType   boolean;

  select geno_display_name, geno_is_wildtype
    into genoName, genoIsWildType
    from genotype
   where geno_zdb_id = genoZdbId;

  if (genoIsWildType = "f") then
      let genoNameHtml = 
	'<span class="mutant" title="'|| genoName || '">' || genoName || '</span>';
  else
      let genoNameHtml = 
	'<span class="wildtype" title="'|| genoName || '">' || genoName || '</span>';
  end if

  return 
    '<a href="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-genotypeview.apg&OID=' ||
      genoZdbId || '">' ||genoNameHtml || '</a>';

end function;

--====================================================================

create function get_geno_name_with_bg_html_link( genoZdbId varchar(50) )
 
  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a genotype, this returns the genotype name
  -- properly formatted and embedded in an HTML link to the geno view page.
  --
  -- INPUT VARS: 
  --   genoZdbId   
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   genotype name with proper HTML formatting, embedded in an HTML link
  --   NULL if genoZdbId does not exist in genotype table
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  define genoName	like genotype.geno_display_name;
  define genoNameHTML	lvarchar;
  define genoIsWildType   boolean;
  define genoBackground lvarchar;
  
  let genoBackground = '';

  select geno_display_name, geno_is_wildtype
    into genoName, genoIsWildType
    from genotype
   where geno_zdb_id = genoZdbId;
     
  select get_genotype_backgrounds(genoZdbId) 
  into genoBackground
  from single;
  
  if (genoBackground != '' AND genoIsWildType = "f") then
     
     let genoName = genoName || ' (' || genoBackground || ')';
     
  end if
  
  
  if (genoIsWildType = "f") then
      let genoNameHtml = 
	'<span class="mutant" title="'|| genoName || '">' || genoName || '</span>';
  else
      let genoNameHtml = 
	'<span class="wildtype" title="'|| genoName || '">' || genoName || '</span>';
  end if

  return 
    '<a href="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-genotypeview.apg&OID=' ||
      genoZdbId || '">' ||genoNameHtml || '</a>';

end function;

--==========================================================================

create function get_genotype_backgrounds( genoZdbId varchar(50) )

  returning lvarchar;	

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a genotype, returns a comma seperated list of 
  -- backgrounds for the genotype.
  --
  --  INPUT VARS: 
  --     genoZdbId   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     Comma seperated list of backgrounds in parenthesis.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  define backName       like genotype.geno_display_name;
  define backgroundList lvarchar;
  
  let backgroundList = '';
  
  foreach
    select geno_display_name
      into backName
      from genotype_background, genotype
     where genoback_geno_zdb_id = genoZdbId
       and genoback_background_zdb_id = geno_zdb_id
    order by geno_display_name
             

      if (backgroundList == '') then
    
          let backgroundList = backName ;
      
      else
  
          let backgroundList = backgroundList ||', '|| backName ;  
  
      end if

  end foreach

  if (backgroundList != '') then
    
          let backgroundList = "(" || backgroundList || ")" ;
  end if 

  return backgroundList;

end function;

--=========================================================================


create function get_genotype_backgrounds_html_link( genoZdbId varchar(50) )

  returning lvarchar;	

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a genotype, returns a comma seperated list of 
  -- backgrounds for the genotype.
  --
  --  INPUT VARS: 
  --     genoZdbId   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     Comma seperated list of backgrounds.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  define backName       varchar(255);
  define backNameDisplay	like genotype.geno_display_name;
  define backgroundList lvarchar;
  
  let backgroundList = 'unspecified';
  
  foreach
    select get_geno_name_html_link(geno_zdb_id), geno_display_name
      into backName, backNameDisplay
      from genotype_background, genotype
     where genoback_geno_zdb_id = genoZdbId
       and genoback_background_zdb_id = geno_zdb_id
    order by geno_display_name
             

      if (backgroundList == 'unspecified') then
    
          let backgroundList = backName ;
      
      else
  
          let backgroundList = backgroundList ||', '|| backName ;  
  
      end if

  end foreach

  return backgroundList;

end function;

--============================================================================

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
  define zygAllele       like zygocity.zyg_allele_display;

  let genoDisplayHtml = '';
  let zygPrefix = '';
      

  foreach
       select distinct mrkr_abbrev, zyg_gene_prefix
         into featAbbrevHtml, zygPrefix
         from marker, genotype_marker, zygocity
        where genomrkr_geno_zdb_id = genoZdbId
          and genomrkr_mrkr_zdb_id = mrkr_zdb_id
          and genomrkr_zygocity = zyg_zdb_id
       order by 1
     
        if (zygPrefix != '') then
          if (genoDisplayHtml == '') then          
            let genoDisplayHtml = zygPrefix ;

          else
            let genoDisplayHtml = genoDisplayHtml ||';'|| zygPrefix ;

          end if          
        end if
        
        
        if (genoDisplayHtml == '' AND zygPrefix == '') then
          let genoDisplayHtml = featAbbrevHtml ;
          
        else
          let genoDisplayHtml = genoDisplayHtml || featAbbrevHtml;
        
        end if
        
  end foreach
  
  foreach
       select distinct get_feature_abbrev_display(feature_zdb_id), zyg_gene_prefix, zyg_allele_display
         into featAbbrevHtml, zygPrefix, zygAllele
         from feature, genotype_feature, zygocity
        where genofeat_geno_zdb_id = genoZdbId
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = zyg_zdb_id
       order by 1
     
        if (zygPrefix != '') then
          if (genoDisplayHtml == '') then          
            let genoDisplayHtml = zygPrefix ;

          else
            let genoDisplayHtml = genoDisplayHtml ||';'|| zygPrefix ;

          end if          
        end if
        
        
        if (genoDisplayHtml == '') then
          let genoDisplayHtml = featAbbrevHtml ;
          
        else
          if ( zygPrefix == '' ) then
            let genoDisplayHtml = genoDisplayHtml ||';'|| featAbbrevHtml;
            
          else
            let genoDisplayHtml = genoDisplayHtml || featAbbrevHtml;
          
          end if        
        end if


        if (zygAllele != '') then
          let genoDisplayHtml = genoDisplayHtml || '<sup>' || zygAllele || '</sup>';
        
        end if
        
  end foreach
        

  return genoDisplayHtml;

end function;

--========================================================================

create function get_genotype_handle( genoZdbId varchar(50) )

  returning lvarchar;	

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a genotype, returns the genotype Handle name in 
  --  format. 
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
  --     genotype Handle with proper  formatting. 
  --     NULL if genoZdbId does not exist in genotype table.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  define genoHandle lvarchar;
  define featAbbrev	 like feature.feature_abbrev;
  define genoBackground  lvarchar;
  define zygFish	 like zygocity.zyg_abbrev;
  define zygMom 	 like zygocity.zyg_abbrev;
  define zygDad 	 like zygocity.zyg_abbrev;
  define featDisplay     lvarchar;

  let genoHandle = '';

  foreach
       select distinct mrkr_abbrev, z1.zyg_abbrev, z2.zyg_abbrev, z3.zyg_abbrev
         into featAbbrev, zygFish, zygMom, zygDad
         from marker, genotype_marker, zygocity z1, zygocity z2, zygocity z3
        where genomrkr_geno_zdb_id = genoZdbId
          and genomrkr_mrkr_zdb_id = mrkr_zdb_id
          and genomrkr_zygocity = z1.zyg_zdb_id
          and genomrkr_mom_zygocity = z2.zyg_zdb_id
          and genomrkr_dad_zygocity = z3.zyg_zdb_id
     order by 1

     let featDisplay = featAbbrev||'['||zygFish||','||zygMom||','||zygDad||']';
     
        if (genoHandle == '') then
          let genoHandle = featDisplay ;
          
        else
          let genoHandle = genoHandle ||' '|| featDisplay ;
        
        end if
        
  end foreach

  foreach
       select distinct feature_abbrev, z1.zyg_abbrev, z2.zyg_abbrev, z3.zyg_abbrev
         into featAbbrev, zygFish, zygMom, zygDad
         from feature, genotype_feature, zygocity z1, zygocity z2, zygocity z3
        where genofeat_geno_zdb_id = genoZdbId
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = z1.zyg_zdb_id
          and genofeat_mom_zygocity = z2.zyg_zdb_id
          and genofeat_dad_zygocity = z3.zyg_zdb_id
     order by 1

     let featDisplay = featAbbrev||'['||zygFish||','||zygMom||','||zygDad||']';
     
        if (genoHandle == '') then
          let genoHandle = featDisplay ;
          
        else
          let genoHandle = genoHandle ||' '|| featDisplay ;
        
        end if
        
  end foreach

  select get_genotype_backgrounds(genoZdbId) 
  into genoBackground
  from single;
  
  if (genoBackground != '') then
     
     let genoHandle = genoHandle || '(' || genoBackground || ')';
     
  end if
  

  return genoHandle;

end function;



commit work ;

--rollback work ;