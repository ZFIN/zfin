drop function get_fish_full_name;

create function
get_fish_full_name(
  fishZdbId varchar(50) )

  returning varchar(180);	-- fish.name + fish.abbrev + 20 misc spaces

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
  -- If the a non-existent fish ZDB ID is passed in then 
  --
  --  UNKNOWN
  --
  -- is returned.

  define fishName	varchar(80);
  define fishAbbrev	varchar(80);
  define fishFullName	varchar(180);
  define fishLineType   varchar(30);

  select name, abbrev, line_type
    into fishName, fishAbbrev, fishLineType
    from fish
    where zdb_id = fishZdbId;

  if (fishName is null) then
    let fishFullName = "UNKNOWN";
  else
    if (fishLineType = "mutant") then
      let fishFullname = "<i>" || fishName || " (" || fishAbbrev || ")</i>";
    else
      let fishFullname = fishName || " (" || fishAbbrev || ")";
    end if
  end if

  return fishFullName;

end function;
