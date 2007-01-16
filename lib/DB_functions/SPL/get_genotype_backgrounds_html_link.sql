
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
  define backHandle		like genotype.geno_handle;
  define backZdbId		like genotype.geno_zdb_id;
  define backgroundList lvarchar;
  
  let backgroundList = 'unspecified';
  
  foreach
    select geno_zdb_id, geno_display_name, geno_handle
      into backZdbId, backNameDisplay, backHandle
      from genotype_background, genotype
     where genoback_geno_zdb_id = genoZdbId
       and genoback_background_zdb_id = geno_zdb_id
    order by geno_display_name
             
      -- if background(wildtype) abbrev is the same as name, then only show name
      if (backNameDisplay == backHandle) then
	let backName = '<a href="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-genotypeview.apg&OID=' ||
		backZdbId || '">' || '<span class="wildtype">' || backNameDisplay || '</span>' || '</a>';
      -- otherwise, show abbrev in parenthesis after name
      else
	let backName = '<a href="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-genotypeview.apg&OID=' ||
		backZdbId || '">' || '<span class="wildtype">' || backNameDisplay || '</span>' || '</a>' ||
		' (' || backHandle || ')';
      end if 
		
      if (backgroundList == 'unspecified') then
    
          let backgroundList = backName ;
      
      else
  
          let backgroundList = backgroundList ||', '|| backName ;  
  
      end if

  end foreach

  return backgroundList;

end function;
