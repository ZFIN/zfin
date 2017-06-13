create or replace function get_entity_name_html ( entityZdbId varchar(50) )

  returns text as $entityNameHtml$;

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

  declare entityNameHtml text;
   entityName text := NULL; 
   entityOntologyId text;
   entityOntologyName text;
   goUrl text;

 begin
  select term_name, term_ont_id, term_ontology
  into entityName, entityOntologyId, entityOntologyName
  from term
  where term_zdb_id = entityZdbId;

   entityNameHtml = '<a href="/' || entityOntologyId 
                       || '">' ||  entityName || '</a>' 
                       || '<a class="popup-link data-popup-link" ' 
                       || 'href="/action/ontology/term-detail-popup?termID=' || entityOntologyId || '"></a>'  ;

  return entityNameHtml;

end
$entityNameHtml$ LANGUAGE plpgsql
