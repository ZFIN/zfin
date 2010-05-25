create function get_postcomposed_term_html ( supertermZdbId varchar(50), 
                                                 subtermZdbId varchar(50)  )

  returning lvarchar;

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of term, 
  -- this returns the name of the entity and a link

  --
  -- INPUT VARS: 
  --   entityZdbId   Get the name of this entity
  -- 
  -- OUTPUT VARS: 
  --   none
  -- 
  -- RETURNS: 
  --   Name of entity with proper HTML formatting.
  --   NULL if entityZdbId does not exist the term table.
  --
  -- EFFECTS:
  --   None
  -- --------------------------------------------------------------------- 

  define resultHtml lvarchar;
  define supertermHtml lvarchar;
  define subtermHtml lvarchar;

  let supertermHtml = get_entity_name_html(supertermZdbId);
  let subtermHtml = get_entity_name_html(subtermZdbId);

  if (subtermHtml is not null) then
    let resultHtml = '<span class=postcomposedtermlink>' || supertermHtml || '&nbsp;' || subtermHtml || '</span>' ;
  else 
    let resultHtml = supertermHtml;
  end if

  return resultHtml;

end function;