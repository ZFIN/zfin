create function get_mrkr_abbrev_html_link( mrkrZdbId varchar(50) )
 
  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a marker, this returns the abbrev of the marker
  -- properly formatted and embedded in an HTML link to the marker view page.
  --
  -- INPUT VARS: 
  --   mrkrZdbId   Get the abbrev of this marker
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   Abbrev of marker with proper HTML formatting, embedded in an HTML link
  --   NULL if mrkrZdbId does not exist in marker table
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  -- A couple of implementation details:
  --  o get_marker_abbrev_html will return NULL if the ZDB ID does not exist.
  --    That will cause the entire return string to be converted to NULL,
  --    which is what we want.
  --  o Currently (2005/04) all marker types are displayed using 
  --    aa-markerview.apg, and therefore we hardcode that below.  If thes
  --    ever changes and different marker types have different display pages
  --    then change this code to get the view app page from the zdb_object_type
  --    table.


  define mrkrAbbrevHtmlLink lvarchar;
  define mrkrAbbrev like marker.mrkr_abbrev;
  define mrkrName like marker.mrkr_name;
  define mrkrType like marker.mrkr_type;

  select mrkr_abbrev, mrkr_name, mrkr_type
    into mrkrAbbrev, mrkrName, mrkrType
    from marker
    where mrkr_zdb_id = mrkrZdbId;

  if mrkrAbbrev is null then
    let mrkrAbbrevHtmlLink = null;
  else
      let mrkrAbbrevHtmlLink = 
          '<a href="' || get_mrkr_url(mrkrZdbId) ||
          '">' || get_mrkr_abbrev_html(mrkrZdbId) || '</a>';
  end if  -- marker exists

  return mrkrAbbrevHtmlLink;

end function;

