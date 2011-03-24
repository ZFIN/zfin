create function get_entity_name_html ( entityZdbId varchar(50) )

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

  define entityNameHtml lvarchar;
  define entityName lvarchar; 
  define entityOntologyId lvarchar;
  define entityOntologyName lvarchar;
  define goUrl lvarchar;

  let entityNameHtml = NULL;

  select term_name, term_ont_id, term_ontology
  into entityName, entityOntologyId, entityOntologyName
  from term
  where term_zdb_id = entityZdbId;

  let entityNameHtml = '<a href="/action/ontology/term-detail?termID=' || entityOntologyId 
                       || '">' ||  entityName || '</a>' 
                       || '<a class="popup-link data-popup-link" ' 
                       || 'href="/action/ontology/term-detail-popup?termID=' || entityOntologyId || '"></a>'  ;

  return entityNameHtml;

end function;