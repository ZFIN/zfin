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

  select term_name into entityName
  from term
  where term_zdb_id = entityZdbId;

  select term_ont_id into entityOntologyId
  from term
  where term_zdb_id = entityZdbId;

  select term_ontology into entityOntologyName
  from term
  where term_zdb_id = entityZdbId;

  if (entityOntologyName = "zebrafish_anatomy") then

     let entityNameHtml = '<a href=/action/anatomy/term-detail?id=' || entityOntologyId || '>' || entityName || '</a>';

  elif (   (entityOntologyName = "biological_process")
        or (entityOntologyName = "cellular_component")
        or (entityOntologyName = "molecular_function") ) then

--    select fdb_db_query into goUrl 
--    from foreign_db 
--    where fdb_db_name = 'QuickGO';

--  selecting from the database gives urls like GO:GO:0000  hardcode for now..

    let goUrl = "http://www.ebi.ac.uk/ego/QuickGO?mode=display&entry=";

    let entityNameHtml = '<a href=' || goUrl || entityOntologyId || '>' || entityName || '</a>';

  else
     let entityNameHtml = entityName;
  end if

  return entityNameHtml;

end function;