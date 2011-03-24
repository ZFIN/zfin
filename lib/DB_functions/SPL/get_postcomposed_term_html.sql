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
  define supertermId lvarchar;
  define supertermName lvarchar;
  define supertermOntology lvarchar;
  define subtermId lvarchar;
  define subtermName lvarchar;
  define subtermOntology lvarchar; 

  select term_name, term_ont_id, term_ontology
  into supertermName, supertermID, supertermOntology
  from term
  where term_zdb_id = supertermZdbId;

  select term_name, term_ont_id, term_ontology
  into subtermName, subtermID, subtermOntology
  from term
  where term_zdb_id = subtermZdbId;


  -- todo: add ontology name as a span title?

  if (subtermID is null) then
    let resultHtml = get_entity_name_html(supertermZdbId);
  else 
    let resultHtml = '<a href="/action/ontology/post-composed-term-detail?superTermID=' 
                     || supertermID || '&subTermID=' || subtermID || '">' 
                     || supertermName || '&nbsp;' || subtermName || '</a>' 
                     || '<a class="popup-link data-popup-link"'
                     || ' href="/action/ontology/post-composed-term-detail-popup?superTermID=' 
                     || supertermID || '&subTermID=' || subtermID || '"></a>';
  end if

  return resultHtml;

end function;