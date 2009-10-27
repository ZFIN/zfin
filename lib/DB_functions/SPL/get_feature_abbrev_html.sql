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
    from feature, outer(feature_marker_relationship, marker)
   where feature_zdb_id = featZdbId
     and fmrel_ftr_zdb_id = feature_zdb_id
     and fmrel_mrkr_zdb_id = mrkr_zdb_id;

  if (featAbbrev is null) then
    let featAbbrevHtml = null;
  else
  
    if (featAbbrev like "%un\_%") then
      let featAbbrev = REPLACE(featAbbrev, featMrkrAbbrev, "unspecified");    
    end if
    
    if (featAbbrev like "%unrec\_%") then
      let featAbbrev = REPLACE(featAbbrev, featMrkrAbbrev, "unrecovered");    
    end if
    
    let featAbbrevHtml = 
	'<span class="mutant" title="'|| featAbbrev || '">' || featAbbrev || '</span>';

  end if  -- feat exists

  return featAbbrevHtml;

end function;
