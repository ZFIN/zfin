create or replace function get_mrkr_abbrev_html( mrkrZdbId varchar(50) )
 
  returns text as $mrkrAbbrevHtml$

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a marker, this returns the abbrev/symbol of the marker
  -- properly italicized (genes, pseudogenes) or not (everything else).
  -- This relies on the invoking page have these styles defined:
  --   genedom
  --   non_genedom_marker
  --
  -- INPUT VARS: 
  --   mrkrZdbId   Get the abbrev/symbol of this marker
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   Abbrev/symbol of marker with proper HTML formatting.
  --   NULL if mrkrZdbId does not exist in marker table
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  declare mrkrAbbrevHtml text;
   mrkrAbbrev  marker.mrkr_abbrev%TYPE;
   mrkrName  marker.mrkr_name%TYPE;
   mrkrType  marker.mrkr_type%TYPE;
   title text;
 begin 
  select mrkr_abbrev, mrkr_name, mrkr_type
    into mrkrAbbrev, mrkrName, mrkrType
    from marker
    where mrkr_zdb_id = mrkrZdbId;

  if mrkrAbbrev is null then
     mrkrAbbrevHtml = null;
  else
--    if lower(mrkrAbbrev) = lower(mrkrName) then
--      let title = '';
--    else
       title = ' title="' || mrkrName || '"';
--    end if
    if exists 
         ( select 'x'
             from marker_type_group_member
             where mtgrpmem_mrkr_type = mrkrType
               and mtgrpmem_mrkr_type_group = "GENEDOM") then
      -- we have a genedom marker, display it as such
       mrkrAbbrevHtml = 
        '<span class="genedom"' || title || '>' || mrkrAbbrev || '</span>';
    else 
      -- we have something else, display it as such
       mrkrAbbrevHtml = 
        '<span class="non_genedom_marker"' || title || '>' || 
          mrkrAbbrev || 
        '</span>';
    end if;
  end if;  -- marker exists

  return mrkrAbbrevHtml;

end
$mrkrAbbrevHtml$ LANGUAGE plpgsql
