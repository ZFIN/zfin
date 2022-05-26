create or replace function get_feature_and_marker_abbrev_display( featZdbId varchar, markerZdbId varchar )

  returns varchar as $featAbbrevHtml$

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a feature and the ZDB ID of the related marker,
  -- returns the feature abbrev as a super script of the gene.
  -- based on get_feature_abbrev_display, but handles features of multiple markers
  --
  --  INPUT VARS: 
  --     featZdbId
  --     markerZdbId
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

  declare featAbbrevHtml  varchar;
   	  featAbbrev      feature.feature_abbrev%TYPE;
   	  featName        feature.feature_name%TYPE;
   	  featMrkrAbbrev  marker.mrkr_abbrev%TYPE := '';
   	  featType        feature.feature_type%TYPE;

begin

for featAbbrev, featMrkrAbbrev, featName, featType in
    select feature_abbrev, mrkr_abbrev, feature_name, feature_type
       from feature left outer join feature_marker_relationship on fmrel_ftr_zdb_id = feature_zdb_id
    	 	 and fmrel_type = 'is allele of'
    	 	 left outer join marker on fmrel_mrkr_zdb_id = mrkr_zdb_id
        where feature_zdb_id = featZdbId
        and (fmrel_mrkr_zdb_id = markerZdbId or fmrel_mrkr_zdb_id is null)
 loop 

  if (featName is null) then
    featAbbrevHtml := null;
  else
  
    if (featName like '%\_unspecified') then
      featName := 'unspecified';    
    end if;
    
    if (featName like '%\_unrecovered') then
      featName := 'unrecovered';    
    end if;

    if (featMrkrAbbrev is null OR featMrkrAbbrev = '') then
        featAbbrevHtml :=  featName ;
    else
        featAbbrevHtml := featMrkrAbbrev || '<sup>' || featName || '</sup>';
    end if;
    
  end if ; -- feat exists

end loop;

  return featAbbrevHtml;

end

$featAbbrevHtml$ LANGUAGE plpgsql;
