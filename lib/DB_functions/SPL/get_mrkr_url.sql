create function get_mrkr_url( mrkrZdbId varchar(50) )
 
  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a marker, this returns the url for the marker
  --
  -- INPUT VARS: 
  --   mrkrZdbId   Get the abbrev of this marker
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   Correct url for marker
  --   NULL if mrkrZdbId does not exist in marker table
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 


  define mrkrUrl lvarchar;

  let mrkrUrl = '/action/marker/view/' || mrkrZdbId ;

  return mrkrUrl;

end function;




