create function get_feature_name_html_link( featureZdbId varchar(50) )
 
  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a feature, this returns the feature name
  -- properly formatted and embedded in an HTML link to the feature view page.
  --
  -- INPUT VARS: 
  --   featureZdbId   
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   feature name with proper HTML formatting, embedded in an HTML link
  --   NULL if featureZdbId does not exist in feature table
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  define featureName	like feature.feature_name;
  define featureNameHTML	lvarchar;

  select feature_name
    into featureName
    from feature
   where feature_zdb_id = featureZdbId;

  let featureNameHTML = 
	'<span class="mutant">' || featureName || '</span>';

  return 
    '<a href="/action/feature/feature-detail?zdbID=' ||
      featureZdbId || '">' ||featureNameHTML || '</a>';

end function;
