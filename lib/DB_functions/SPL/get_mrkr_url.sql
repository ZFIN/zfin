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
  define mrkrType like marker.mrkr_type;
  define title lvarchar;

  select mrkr_type
    into mrkrType
    from marker
    where mrkr_zdb_id = mrkrZdbId;

  if mrkrType is null then
    let mrkrUrl = null;
  else
    if exists 
         ( select 'x'
             from marker_type_group_member
             where mtgrpmem_mrkr_type = mrkrType
               and mtgrpmem_mrkr_type_group = "TRANSCRIPT") then
      let mrkrUrl = '/action/marker/transcript-view?zdbID=' || mrkrZdbId ;
    elif exists 
         ( select 'x'
             from marker_type_group_member
             where mtgrpmem_mrkr_type = mrkrType
               and mtgrpmem_mrkr_type_group = "ATB") then
      let mrkrUrl = 
          '/action/antibody/detail?antibody.zdbID=' || mrkrZdbId ;

--    elif exists 
--         ( select 'x'
--             from marker_type_group_member
--             where mtgrpmem_mrkr_type = mrkrType
--               and mtgrpmem_mrkr_type_group in ("CLONE","SMALLSEG")) then
      -- we have a genedom marker, display it as such
--      let mrkrUrl = 
--          '<a href="/action/marker/clone-view?zdbID=' || mrkrZdbId ||
--          '">' || get_mrkr_abbrev_html(mrkrZdbId) || '</a>';
    else 
      let mrkrUrl = 
             '/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-markerview.apg&OID=' ||
      	     mrkrZdbId ;
    end if
  end if  -- marker exists

  return mrkrUrl;

end function;




