create function get_fish_abbrev_html( fishZdbId varchar(50) )

  returning lvarchar;	

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a fish, returns the fish abbrev properly italicized 
  -- (mutant) or not (wildtype). This relies on the invoking page have these
  -- style defined. 
  --   mutant
  --   wildtype
  --
  --  INPUT VARS: 
  --     fishZdbId   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     fish abbrev with proper HTML formatting. 
  --     NULL if fishZdbId does not exist in fish table.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  define fishAbbrevHtml  lvarchar;
  define fishName	like fish.name;	
  define fishAbbrev	like fish.abbrev;
  define fishLineType   like fish.line_type;
  define fishAllele     like fish.allele;

  select name, abbrev, line_type, allele
    into fishName, fishAbbrev, fishLineType, fishAllele
    from fish
   where zdb_id = fishZdbId;

  if (fishAbbrev is null) then
    let fishAbbrevHtml = null;
  else
  
    if (fishAbbrev like "%un\_%") then
      let fishAbbrev = REPLACE(fishAbbrev, fishAllele, "unspecified");    
    end if
    
    if (fishLineType = "mutant") then
      let fishAbbrevHtml = 
	'<span class="mutant" title="'|| fishName || '">' || fishAbbrev || '</span>';
    else
      let fishAbbrevHtml = 
	'<span class="wildtype" title="'|| fishName || '">' || fishAbbrev || '</span>';
    end if
  end if  -- fish exists

  return fishAbbrevHtml;

end function;
