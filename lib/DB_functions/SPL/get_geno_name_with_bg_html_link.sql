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
  
 
  if (genoIsWildType = "f") then
      let genoNameHtml = 
	'<span class="mutant">' || genoName || '</span>';
  else
      let genoNameHtml = 
	'<span class="wildtype">' || genoName || '</span>';
  end if
  
  if (genoBackground != '') then
     
     let genoNameHtml = genoNameHtml || ' (' || genoBackground || ')';
     
  end if

  return 
    '<a href="/action/genotype/genotype-detail?zdbID=' ||
      genoZdbId || '">' ||genoNameHtml || '</a>';

end function;
