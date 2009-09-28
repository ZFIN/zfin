create function get_mrkr_name_html_link( mrkrZdbId varchar(50) )
 
  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a marker, this returns the name of the marker
  -- properly formatted and embedded in an HTML link to the marker view page.
  --
  -- INPUT VARS: 
  --   mrkrZdbId   Get the name of this marker
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   Name of marker with proper HTML formatting, embedded in an HTML link
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 



  return
    '<a href="' || get_mrkr_url(mrkrZdbId) || '">' ||
      get_mrkr_name_html(mrkrZdbId) ||
    '</a>';

end function;
