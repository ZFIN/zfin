create function get_mrkr_name_html( mrkrZdbId varchar(50) )
 
  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a marker, this returns the name of the marker
  -- properly italicized (genes, pseudogenes) or not (everything else).
  -- This relies on the invoking page have these styles defined:
  --   genedom
  --   non_genedom_marker
  --
  -- INPUT VARS: 
  --   mrkrZdbId   Get the name of this marker
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   Name of marker with proper HTML formatting.
  --   NULL if mrkrZdbId does not exist in marker table
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  define mrkrNameHtml lvarchar;
  define mrkrName like marker.mrkr_name;
  define mrkrAbbrev like marker.mrkr_abbrev;
  define mrkrType like marker.mrkr_type;
  define title lvarchar;

  select mrkr_name, mrkr_abbrev, mrkr_type
    into mrkrName, mrkrAbbrev, mrkrType
    from marker
    where mrkr_zdb_id = mrkrZdbId;

  if mrkrName is null then
    let mrkrNameHtml = null;
  else
    let title = ' title="' || mrkrAbbrev || '"';
    if exists 
         ( select 'x'
             from marker_type_group_member
             where mtgrpmem_mrkr_type = mrkrType
               and mtgrpmem_mrkr_type_group = "GENEDOM") then
      -- we have a genedom marker, display it as such
      let mrkrNameHtml = '<span class="genedom"' || title || '>' || mrkrName || '</span>';
    else 
      -- we have something else, display it as such
      let mrkrNameHtml = '<span class="nongenedommarker"' || title || '>' || mrkrName || '</span>';
    end if
    if exists 
         ( select 'x'
             from marker_type_group_member
             where mtgrpmem_mrkr_type = mrkrType
               and mtgrpmem_mrkr_type_group = "CONSTRUCT") then
      -- we have a genedom marker, display it as such
      let mrkrNameHtml = '<span class="construct"' || title || '>' || mrkrName || '</span>';
    end if
  end if  -- marker exists

  return mrkrNameHtml;

end function;
