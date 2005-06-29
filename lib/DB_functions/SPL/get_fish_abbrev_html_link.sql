create function get_fish_abbrev_html_link( fishZdbId varchar(50) )
 
  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a fish, this returns the abbrev of the fish
  -- properly formatted and embedded in an HTML link to the fish view page.
  --
  -- INPUT VARS: 
  --   fishZdbId   
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   Abbrev of fish with proper HTML formatting, embedded in an HTML link
  --   NULL if fishZdbId does not exist in fish table
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  -- A couple of implementation details:
  --  o the routinel will return NULL if the ZDB ID does not exist.
  --    That will cause the entire return string to be converted to NULL,
  --    which is what we want.

  return
    '<a href="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-fishview.apg&OID=' ||
      fishZdbId || '">' ||
      get_fish_abbrev_html(fishZdbId) ||
    '</a>';

end function;
