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
  
  let featMrkrAbbrev = "";

foreach
  select feature_abbrev, mrkr_abbrev, feature_name
    into featAbbrev, featMrkrAbbrev, featName
    from feature, outer(feature_marker_relationship, marker)
   where feature_zdb_id = featZdbId
     and fmrel_ftr_zdb_id = feature_zdb_id
     and fmrel_mrkr_zdb_id = mrkr_zdb_id
     and fmrel_type not like "contains%"
     

  if (featName is null) then
    let featAbbrevHtml = null;
  else
  
    if (featName like "%un\_%") then
      let featName = "unspecified";    
    end if
    
    if (featMrkrAbbrev is null OR featMrkrAbbrev == '') then
        let featAbbrevHtml =  featName ;
       
    else
        let featAbbrevHtml = featMrkrAbbrev || '<sup>' || featName || '</sup>';

    end if
    
  end if  -- feat exists

end foreach

  return featAbbrevHtml;

end function;
