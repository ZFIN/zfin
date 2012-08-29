create function get_ao_name_html_link( aoZdbId varchar(50) )
 
  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of an AO, this returns the AO name
  -- properly formatted and embedded in an HTML link to the AO view page.
  --
  -- INPUT VARS: 
  --   aoZdbId   
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   AO name with proper HTML formatting, embedded in an HTML link
  --   NULL if aoZdbId does not exist in feature table
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  define aoName	like anatomy_item.anatitem_name;
  define aoNameHTML	lvarchar;

  select anatitem_name
    into aoName
    from anatomy_item
   where anatitem_zdb_id = aoZdbId;

  let aoNameHTML = 
	'<span class="mutant">' || aoName || '</span>';

  return 
    '<a href="/action/anatomy/anatomy-view/' ||
      aoZdbId || '">' ||aoNameHTML || '</a>';

end function;
