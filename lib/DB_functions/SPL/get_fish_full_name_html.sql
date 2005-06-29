create function get_fish_full_name_html( fishZdbId varchar(50) )

  returning lvarchar;	-- fish.name + fish.abbrev + 20 misc spaces

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a fish, returns a fully qualified fish name of
  -- the form:
  --
  --  Mutants:   <i>locus_name (locus_abbrev<sup>allele_name</sup>)</i>
  --  Wildtypes: wildtype_name (wildtype_abbrev)
  --
  -- However, for mutants, it doesn't go to locus to get this information.  
  -- Instead it pulls it from fields that are in the fish record.
  -- What really gets returned (for both mutants and wildtypes) is:
  --
  --  fish.name (fish.abbrev)
  --
  --  INPUT VARS: 
  --     fishZdbId   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     fish full name with proper HTML formatting. 
  --     NULL if fishZdbId does not exist in fish table.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 

  define fishFullNameHtml  lvarchar;
  define fishName	like fish.name;
  define fishAbbrev	like fish.abbrev;
  define fishLineType   like fish.line_type;

  select name, abbrev, line_type
    into fishName, fishAbbrev, fishLineType
    from fish
    where zdb_id = fishZdbId;

  if (fishName is null) then
    let fishFullNameHtml = null;
  else
    if (fishLineType = "mutant") then
      let fishFullNameHtml = 
	'<span class="mutant">' || fishName || ' (' || fishAbbrev || ') </span>';
    else
      let fishFullNameHtml = 
	'<span class="wildtype">' || fishName || ' (' || fishAbbrev || ') </span>';
    end if
  end if  -- fish exists

  return fishFullNameHtml;

end function;
