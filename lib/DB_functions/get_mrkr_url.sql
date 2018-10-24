create or replace function get_mrkr_url( mrkrZdbId text )
 
  returns text as $mrkrUrl$

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


 declare mrkrUrl text := mrkrUrl = '/' || mrkrZdbId ;
 begin 
  return mrkrUrl;
 end
 $mrkrUrl$ LANGUAGE plpgsql



